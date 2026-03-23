package ru.shvalieva.collector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.shvalieva.dto.AgentDataDto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class SystemCollector {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    public AgentDataDto collect(String hostId, String token) {
        AgentDataDto data = new AgentDataDto();
        data.setHostId(hostId);
        data.setToken(token);
        data.setTimestamp(ISO_FORMATTER.format(Instant.now()));

        // Сбор информации об ОС
        data.setOsInfo(collectOsInfo());

        // Версия ядра
        data.setKernelVersion(collectKernelVersion());

        // Архитектура
        data.setArchitecture(collectArchitecture());

        // Список установленных пакетов
        data.setPackages(collectPackages());

        // Список репозиториев
        data.setRepositories(collectRepositories());

        // Список модулей ядра
        data.setModules(collectModules());

        return data;
    }

    private Map<String, String> collectOsInfo() {
        Map<String, String> osInfo = new HashMap<>();
        Path osRelease = Paths.get("/etc/os-release");
        if (Files.exists(osRelease)) {
            try (Stream<String> lines = Files.lines(osRelease)) {
                lines.map(line -> line.split("=", 2))
                        .filter(parts -> parts.length == 2)
                        .forEach(parts -> osInfo.put(parts[0], parts[1].replace("\"", "")));
            } catch (IOException e) {
                log.error("Не удалось прочитать /etc/os-release", e);
            }
        } else {
            osInfo.put("NAME", executeCommand("uname -s"));
            osInfo.put("VERSION", executeCommand("uname -r"));
        }
        return osInfo;
    }

    private String collectKernelVersion() {
        return executeCommand("uname -r");
    }

    private String collectArchitecture() {
        return executeCommand("uname -m");
    }

    private List<AgentDataDto.PackageDto> collectPackages() {
        List<AgentDataDto.PackageDto> packages = new ArrayList<>();
        // Пытаемся определить, какой пакетный менеджер используется
        if (isCommandAvailable("dpkg-query")) {
            // Debian/Ubuntu/Astra
            String output = executeCommand("dpkg-query -W -f='${Package}\\t${Version}\\t${Architecture}\\n'");
            if (output != null && !output.isEmpty()) {
                for (String line : output.split("\n")) {
                    String[] parts = line.trim().split("\t");
                    if (parts.length >= 3) {
                        AgentDataDto.PackageDto pkg = new AgentDataDto.PackageDto();
                        pkg.setName(parts[0]);
                        pkg.setVersion(parts[1]);
                        pkg.setArchitecture(parts[2]);
                        packages.add(pkg);
                    }
                }
            }
        } else {
            log.warn("Не найден поддерживаемый менеджер пакетов (dpkg)");
        }
        return packages;
    }

    private List<AgentDataDto.RepositoryDto> collectRepositories() {
        List<AgentDataDto.RepositoryDto> repos = new ArrayList<>();
        // APT репозиторий
        if (Files.exists(Paths.get("/etc/apt"))) {
            collectAptRepositories(repos);
        }
        return repos;
    }

    private void collectAptRepositories(List<AgentDataDto.RepositoryDto> repos) {
        // Чтение основного файла sources.list
        Path sourcesList = Paths.get("/etc/apt/sources.list");
        if (Files.exists(sourcesList)) {
            parseAptSourcesFile(sourcesList, repos);
        }
        // Чтение файлов из sources.list.d
        Path sourcesListD = Paths.get("/etc/apt/sources.list.d");
        if (Files.isDirectory(sourcesListD)) {
            try (Stream<Path> files = Files.list(sourcesListD)) {
                files.filter(p -> p.toString().endsWith(".list"))
                        .forEach(p -> parseAptSourcesFile(p, repos));
            } catch (IOException e) {
                log.error("Не удалось прочитать /etc/apt/sources.list.d", e);
            }
        }
    }

    private void parseAptSourcesFile(Path file, List<AgentDataDto.RepositoryDto> repos) {
        try (Stream<String> lines = Files.lines(file)) {
            lines.map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .forEach(line -> {
                        // Формат: deb uri distribution [components...]
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 3 && (parts[0].equals("deb") || parts[0].equals("deb-src"))) {
                            AgentDataDto.RepositoryDto repo = new AgentDataDto.RepositoryDto();
                            repo.setName(parts[1]);
                            repo.setUrl(parts[1]);
                            repo.setDistribution(parts[2]);
                            List<String> components = new ArrayList<>();
                            for (int i = 3; i < parts.length; i++) {
                                components.add(parts[i]);
                            }
                            repo.setComponents(components);
                            repos.add(repo);
                        }
                    });
        } catch (IOException e) {
            log.error("Не удалось разобрать исходный файл apt: {}", file, e);
        }
    }

    private List<AgentDataDto.ModuleDto> collectModules() {
        List<AgentDataDto.ModuleDto> modules = new ArrayList<>();
        String lsmodOutput = executeCommand("lsmod");
        if (lsmodOutput != null && !lsmodOutput.isEmpty()) {
            String[] lines = lsmodOutput.split("\n");
            // Пропускаем заголовок
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                if (parts.length > 0) {
                    String moduleName = parts[0];
                    AgentDataDto.ModuleDto module = new AgentDataDto.ModuleDto();
                    module.setName(moduleName);
                    // Получаем версию модуля через modinfo
                    String version = getModuleVersion(moduleName);
                    module.setVersion(version != null ? version : "unknown");
                    modules.add(module);
                }
            }
        }
        return modules;
    }

    private String getModuleVersion(String moduleName) {
        String output = executeCommand("modinfo " + moduleName + " | grep -i '^version:'");
        if (output != null && !output.isEmpty()) {
            String[] parts = output.split(":\\s+", 2);
            if (parts.length == 2) {
                return parts[1].trim();
            }
        }
        return null;
    }

    private String executeCommand(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            if (command.contains("|")) {
                // Сложные команды с пайпом нужно выполнять через shell
                pb.command("sh", "-c", command);
            } else {
                pb.command(command.split("\\s+"));
            }
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String result = reader.lines().collect(Collectors.joining("\n"));
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    return result;
                } else {
                    log.warn("Команда '{}' завершилась с кодом {}", command, exitCode);
                    return null;
                }
            }
        } catch (Exception e) {
            log.error("Не удалось выполнить команду: {}", command, e);
            return null;
        }
    }

    private boolean isCommandAvailable(String command) {
        String which = executeCommand("which " + command);
        return which != null && !which.isEmpty();
    }
}