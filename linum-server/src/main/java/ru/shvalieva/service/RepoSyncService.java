package ru.shvalieva.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import ru.shvalieva.model.*;
import ru.shvalieva.repository.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.*;
import java.util.zip.GZIPInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepoSyncService {

    private final RepositoryRepository repositoryRepository;
    private final PackageEntityRepository packageEntityRepository;
    private final RepoPackageRepository repoPackageRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    // Запуск раз в сутки
   @Scheduled(cron = "0 0 3 * * ?") // каждый день в 8 утра
    @Transactional
    public void syncAllRepositories() {
        List<Repository> repositories = repositoryRepository.findAll();
        for (Repository repo : repositories) {
            syncRepository(repo);
        }
    }

    private void syncRepository(Repository repo) {
        log.info("Синхронизация репозитория: {} ({})", repo.getName(), repo.getUrl());
        // Для APT-репозитория ожидаем структуру: {url}/dists/{distribution}/{component}/binary-{arch}/Packages.gz
        //TODO: архитектура amd64 (можно потом брать из таблицы host или конфигурации)
        String arch = "amd64";
        List<String> components = new ArrayList<>();
        if (repo.getComponents() != null && !repo.getComponents().isEmpty()) {
            components = Arrays.asList(repo.getComponents().split(","));
        } else {
            // Если компоненты не заданы, попробуем "main" (в репозитории может быть несколько компонентов)
            components = Collections.singletonList("main");
        }

        for (String component : components) {
            String url = repo.getUrl() + "/dists/" + repo.getDistribution() + "/" + component + "/binary-" + arch + "/Packages.gz";
            try {
                log.debug("Загрузка метаданных из: {}", url);
                ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    parsePackagesGz(response.getBody(), repo);
                } else {
                    log.warn("Не удалось загрузить метаданные для {}: {}", url, response.getStatusCode());
                }
            } catch (Exception e) {
                log.error("Ошибка при загрузке {}: {}", url, e.getMessage());
            }
        }
    }

    // TODO: вынести в фоновую задачу
    private void parsePackagesGz(byte[] gzData, Repository repo) {
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(gzData));
             BufferedReader reader = new BufferedReader(new InputStreamReader(gzip))) {
            String line;
            PackageEntity currentPackage = null;
            String version = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Package: ")) {
                    if (currentPackage != null && version != null) {
                        saveRepoPackage(repo, currentPackage, version);
                    }
                    String name = line.substring(9).trim();
                    currentPackage = packageEntityRepository.findByName(name)
                            .orElseGet(() -> {
                                PackageEntity p = new PackageEntity();
                                p.setName(name);
                                return packageEntityRepository.save(p);
                            });
                    version = null;
                } else if (line.startsWith("Version: ")) {
                    version = line.substring(9).trim();
                }
            }
            if (currentPackage != null && version != null) {
                saveRepoPackage(repo, currentPackage, version);
            }
        } catch (Exception e) {
            log.error("Ошибка парсинга Packages.gz: {}", e.getMessage());
        }
    }

    private void saveRepoPackage(Repository repo, PackageEntity pkg, String version) {
        Optional<RepoPackage> existing = repoPackageRepository.findByRepositoryAndPackageEntity(repo, pkg);
        if (existing.isPresent()) {
            RepoPackage rp = existing.get();
            if (!rp.getLatestVersion().equals(version)) {
                rp.setLatestVersion(version);
                rp.setUpdatedAt(Instant.now());
                repoPackageRepository.save(rp);
                log.debug("Обновлена версия пакета {} в репозитории {}: {}", pkg.getName(), repo.getName(), version);
            }
        } else {
            RepoPackage rp = new RepoPackage();
            rp.setRepository(repo);
            rp.setPackageEntity(pkg);
            rp.setLatestVersion(version);
            rp.setUpdatedAt(Instant.now());
            repoPackageRepository.save(rp);
            log.debug("Добавлен пакет {} в репозиторий {} с версией {}", pkg.getName(), repo.getName(), version);
        }
    }
}