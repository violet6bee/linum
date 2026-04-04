package ru.shvalieva.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shvalieva.model.*;
import ru.shvalieva.repository.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpgradeService {

    private final HostPackageRepository hostPackageRepository;
    private final RepoPackageRepository repoPackageRepository;
    private final HostUpgradableRepository hostUpgradableRepository;

    @Transactional
    public void computeOutdatedPackagesForHost(Host host) {
        List<HostPackage> hostPackages = hostPackageRepository.findByHost(host);
        if (hostPackages.isEmpty()) {
            log.debug("Нет пакетов для хоста {}", host.getName());
            return;
        }

        // Для каждого уникального пакета ищем максимальную версию в репозиториях
        Map<String, String> latestVersions = new HashMap<>();
        for (HostPackage hp : hostPackages) {
            PackageEntity pkg = hp.getPackageEntity();
            if (!latestVersions.containsKey(pkg.getName())) {
                List<RepoPackage> repoPkgs = repoPackageRepository.findAllByPackageEntity(pkg);
                if (!repoPkgs.isEmpty()) {
                    String maxVersion = repoPkgs.stream()
                            .map(RepoPackage::getLatestVersion)
                            .max(String::compareTo)
                            .orElse(null);
                    if (maxVersion != null) {
                        latestVersions.put(pkg.getName(), maxVersion);
                    }
                }
            }
        }

        // Формируем список устаревших пакетов
        List<HostUpgradable> upgradableList = new ArrayList<>();
        for (HostPackage hp : hostPackages) {
            String latest = latestVersions.get(hp.getPackageEntity().getName());
            if (latest != null && !latest.equals(hp.getVersion())) {
                HostUpgradable upgradable = new HostUpgradable();
                upgradable.setHost(host);
                upgradable.setPackageEntity(hp.getPackageEntity());
                upgradable.setCurrentVersion(hp.getVersion());
                upgradable.setTargetVersion(latest);
                upgradable.setDiscoveredAt(Instant.now());
                upgradableList.add(upgradable);
            }
        }

        // Удаляем старые записи для этого хоста и сохраняем новые
        hostUpgradableRepository.deleteByHost(host);
        if (!upgradableList.isEmpty()) {
            hostUpgradableRepository.saveAll(upgradableList);
            log.info("Для хоста {} найдено {} устаревших пакетов", host.getName(), upgradableList.size());
        } else {
            log.debug("Для хоста {} нет устаревших пакетов", host.getName());
        }
    }
}