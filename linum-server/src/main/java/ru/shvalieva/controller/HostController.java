package ru.shvalieva.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.shvalieva.dto.HostDetailsDto;
import ru.shvalieva.dto.HostSummaryDto;
import ru.shvalieva.dto.StatsDto;
import ru.shvalieva.model.Host;
import ru.shvalieva.model.HostPackage;
import ru.shvalieva.model.HostUpgradable;
import ru.shvalieva.repository.HostPackageRepository;
import ru.shvalieva.repository.HostRepository;
import ru.shvalieva.repository.HostUpgradableRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class HostController {

    private final HostRepository hostRepository;
    private final HostPackageRepository hostPackageRepository;
    private final HostUpgradableRepository hostUpgradableRepository;

    @GetMapping("/hosts")
    public ResponseEntity<List<HostSummaryDto>> getHosts() {
        List<Host> hosts = hostRepository.findAll();
        List<HostSummaryDto> result = hosts.stream().map(host -> {
            int outdatedCount = hostUpgradableRepository.findByHost(host).size();
            return HostSummaryDto.builder()
                    .id(host.getId())
                    .name(host.getName())
                    .osPrettyName(host.getOsPrettyName())
                    .kernelVersion(host.getKernelVersion())
                    .lastUpdated(host.getLastUpdated())
                    .outdatedCount(outdatedCount)
                    .build();
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/hosts/{id}")
    public ResponseEntity<HostDetailsDto> getHostDetails(@PathVariable UUID id) {
        Host host = hostRepository.findById(id).orElse(null);
        if (host == null) {
            return ResponseEntity.notFound().build();
        }
        // Список установленных пакетов
        List<HostPackage> hostPackages = hostPackageRepository.findByHost(host);
        List<HostDetailsDto.PackageInfo> packages = hostPackages.stream()
                .map(hp -> HostDetailsDto.PackageInfo.builder()
                        .name(hp.getPackageEntity().getName())
                        .version(hp.getVersion())
                        .architecture(hp.getArchitecture())
                        .build())
                .collect(Collectors.toList());

        // Список пакетов, доступных к обновлению
        List<HostUpgradable> upgradable = hostUpgradableRepository.findByHost(host);
        List<HostDetailsDto.UpgradablePackageInfo> upgradablePackages = upgradable.stream()
                .map(up -> HostDetailsDto.UpgradablePackageInfo.builder()
                        .name(up.getPackageEntity().getName())
                        .currentVersion(up.getCurrentVersion())
                        .targetVersion(up.getTargetVersion())
                        .build())
                .collect(Collectors.toList());

        HostDetailsDto dto = HostDetailsDto.builder()
                .id(host.getId())
                .name(host.getName())
                .ipAddress(host.getIpAddress())
                .osPrettyName(host.getOsPrettyName())
                .kernelVersion(host.getKernelVersion())
                .architecture(host.getArchitecture())
                .lastUpdated(host.getLastUpdated())
                .packages(packages)
                .upgradablePackages(upgradablePackages)
                .build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsDto> getStats() {
        long totalHosts = hostRepository.count();
        // Подсчёт хостов, у которых есть хотя бы один устаревший пакет
        List<Host> allHosts = hostRepository.findAll();
        long outdatedHosts = allHosts.stream()
                .filter(host -> !hostUpgradableRepository.findByHost(host).isEmpty())
                .count();
        // Распределение по ОС
        Map<String, Long> osCount = allHosts.stream()
                .map(Host::getOsPrettyName)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        List<StatsDto.OsDistribution> osDistribution = osCount.entrySet().stream()
                .map(e -> StatsDto.OsDistribution.builder().os(e.getKey()).count(e.getValue()).build())
                .collect(Collectors.toList());

        StatsDto stats = StatsDto.builder()
                .totalHosts(totalHosts)
                .outdatedHosts(outdatedHosts)
                .osDistribution(osDistribution)
                .build();
        return ResponseEntity.ok(stats);
    }
}