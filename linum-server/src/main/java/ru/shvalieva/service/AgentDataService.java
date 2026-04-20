package ru.shvalieva.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shvalieva.dto.AgentDataDto;
import ru.shvalieva.model.*;
import ru.shvalieva.model.ModuleEntity;
import ru.shvalieva.model.PackageEntity;
import ru.shvalieva.repository.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentDataService {

    private final HostRepository hostRepository;
    private final PackageEntityRepository packageEntityRepository;
    private final HostPackageRepository hostPackageRepository;
    private final RepositoryRepository repositoryRepository;
    private final HostRepoRepository hostRepoRepository;
    private final ModuleEntityRepository moduleEntityRepository;
    private final HostModuleRepository hostModuleRepository;
    private final UpgradeService upgradeService;

    @Transactional
    public void processAgentData(AgentDataDto dto, String ipAddress) {
        // 1. Найти или создать хост по токену, сразу сохраняя новый
        String token = dto.getToken();
        Host host = hostRepository.findByToken(token)
                .orElseGet(() -> {
                    Host newHost = createNewHost(dto);
                    return hostRepository.save(newHost); // Сохраняем новый хост
                });
        if (host.getFirstSeen() == null) {
            host.setFirstSeen(Instant.now());
        }
        // 2. Обновить информацию о хосте (для уже существующего или только что сохранённого)
        updateHostInfo(host, dto, ipAddress);

        // 3. Обработать пакеты
        if (dto.getPackages() != null) {
            processPackages(host, dto.getPackages());
        }

        // 4. Обработать репозитории
        if (dto.getRepositories() != null) {
            processRepositories(host, dto.getRepositories());
        }

        // 5. Обработать модули
        if (dto.getModules() != null) {
            processModules(host, dto.getModules());
        }

        // Обновить время последнего обновления (хост уже управляемый)
        host.setLastUpdated(Instant.now());
        hostRepository.save(host); // Сохраняем изменения (можно не вызывать, если хост уже в контексте, но для надёжности оставим)
        upgradeService.computeOutdatedPackagesForHost(host);
    }

    private Host createNewHost(AgentDataDto dto) {
        Host host = new Host();
        host.setToken(dto.getToken());
        host.setFirstSeen(Instant.now());
        return host;
    }

    private void updateHostInfo(Host host, AgentDataDto dto, String ipAddress) {
        // Используем hostId как имя хоста (можно улучшить)
        host.setName(dto.getHostId());
        host.setIpAddress(ipAddress);
        if (dto.getOsInfo() != null) {
            // Просто сохраняем как строку для начала
            host.setOsInfo(dto.getOsInfo().toString());
            String pretty = extractPrettyName(dto.getOsInfo());
            log.info("Извлечение имени: {}", pretty);
            host.setOsPrettyName(pretty);
        }
        host.setKernelVersion(dto.getKernelVersion());
        host.setArchitecture(dto.getArchitecture());
    }

    private String extractPrettyName(Map<String, String> osInfo) {
        if (osInfo == null) return "Неизвестная ОС";
        String pretty = osInfo.get("PRETTY_NAME");
        if (pretty != null && !pretty.isEmpty()) {
            return pretty;
        }
        String name = osInfo.get("NAME");
        String version = osInfo.get("VERSION_ID");
        if (name != null && version != null) {
            return name + " " + version;
        }
        if (name != null) return name;
        return "Неизвестная ОС";
    }

    private void processPackages(Host host, List<AgentDataDto.PackageDto> packages) {
        for (AgentDataDto.PackageDto pkgDto : packages) {
            // Найти или создать пакет в справочнике
            PackageEntity packageEntity = packageEntityRepository.findByName(pkgDto.getName())
                    .orElseGet(() -> {
                        PackageEntity newPkg = new PackageEntity();
                        newPkg.setName(pkgDto.getName());
                        return packageEntityRepository.save(newPkg);
                    });

            // Найти существующую запись HostPackage
            Optional<HostPackage> existing = hostPackageRepository.findByHostAndPackageEntity(host, packageEntity);
            if (existing.isPresent()) {
                HostPackage hp = existing.get();
                // Если версия или архитектура изменились, обновляем
                if (!hp.getVersion().equals(pkgDto.getVersion()) ||
                        !hp.getArchitecture().equals(pkgDto.getArchitecture())) {
                    hp.setVersion(pkgDto.getVersion());
                    hp.setArchitecture(pkgDto.getArchitecture());
                    hp.setUpdatedAt(Instant.now());
                    hostPackageRepository.save(hp);
                }
            } else {
                HostPackage hp = new HostPackage();
                hp.setHost(host);
                hp.setPackageEntity(packageEntity);
                hp.setVersion(pkgDto.getVersion());
                hp.setArchitecture(pkgDto.getArchitecture());
                hp.setUpdatedAt(Instant.now());
                hostPackageRepository.save(hp);
            }
        }
    }

    private void processRepositories(Host host, List<AgentDataDto.RepositoryDto> repositories) {
        for (AgentDataDto.RepositoryDto repoDto : repositories) {
            // Найти или создать репозиторий по URL
            Repository repo = repositoryRepository.findByUrl(repoDto.getUrl())
                    .orElseGet(() -> {
                        Repository newRepo = new Repository();
                        newRepo.setName(repoDto.getName());
                        newRepo.setUrl(repoDto.getUrl());
                        newRepo.setDistribution(repoDto.getDistribution());
                        if (repoDto.getComponents() != null) {
                            newRepo.setComponents(String.join(",", repoDto.getComponents()));
                        }
                        newRepo.setType("APT"); // Можно передавать в DTO, пока фиксировано
                        return repositoryRepository.save(newRepo);
                    });

            // Проверить, есть ли уже связь хоста с этим репозиторием
            boolean alreadyLinked = hostRepoRepository.findByHost(host).stream()
                    .anyMatch(hr -> hr.getRepository().equals(repo));
            if (!alreadyLinked) {
                HostRepo hr = new HostRepo();
                hr.setHost(host);
                hr.setRepository(repo);
                hr.setDiscoveredAt(Instant.now());
                hostRepoRepository.save(hr);
            }
        }
    }

    private void processModules(Host host, List<AgentDataDto.ModuleDto> modules) {
        for (AgentDataDto.ModuleDto modDto : modules) {
            // Найти или создать модуль в справочнике
            ModuleEntity moduleEntity = moduleEntityRepository.findByName(modDto.getName())
                    .orElseGet(() -> {
                        ModuleEntity newMod = new ModuleEntity();
                        newMod.setName(modDto.getName());
                        return moduleEntityRepository.save(newMod);
                    });

            // Найти существующую запись HostModule
            Optional<HostModule> existing = hostModuleRepository.findByHostAndModuleEntity(host, moduleEntity);
            if (existing.isPresent()) {
                HostModule hm = existing.get();
                if (!hm.getVersion().equals(modDto.getVersion())) {
                    hm.setVersion(modDto.getVersion());
                    hm.setUpdatedAt(Instant.now());
                    hostModuleRepository.save(hm);
                }
            } else {
                HostModule hm = new HostModule();
                hm.setHost(host);
                hm.setModuleEntity(moduleEntity);
                hm.setVersion(modDto.getVersion());
                hm.setUpdatedAt(Instant.now());
                hostModuleRepository.save(hm);
            }
        }
    }
}