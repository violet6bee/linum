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
            String output = executeCommand("dpkg-query -W -f='${Package}\\t${Version}\\t${Architecture}\\n'");
            if (output != null && !output.isEmpty()) {
                for (String line : output.split("\n")) {
                    String[] parts = line.trim().split("\t");
                    if (parts.length >= 3) {
                        AgentDataDto.PackageDto pkg = new AgentDataDto.PackageDto();
                        pkg.setName(parts[0].replaceAll("^['\"]+", "").replaceAll("['\"]+$", ""));
                        pkg.setVersion(parts[1].replaceAll("^['\"]+", "").replaceAll("['\"]+$", ""));
                        pkg.setArchitecture(parts[2].replaceAll("^['\"]+", "").replaceAll("['\"]+$", ""));
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
                files.forEach(p -> {
                    if (p.toString().endsWith(".list")) {
                        parseAptSourcesFile(p, repos);
                    } else if (p.toString().endsWith(".sources")) {
                        parseDeb822SourcesFile(p, repos);
                    }
                });
            } catch (IOException e) {
                log.error("Failed to read /etc/apt/sources.list.d", e);
            }
        }
    }

    // Парсер старых .list файлов
    private void parseAptSourcesFile(Path file, List<AgentDataDto.RepositoryDto> repos) {
        try (Stream<String> lines = Files.lines(file)) {
            for (String line : (Iterable<String>) lines::iterator) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");
                if (parts.length < 3) continue;

                if (!parts[0].equals("deb")) continue;

                // Формат: deb uri distribution [components...]
                int idx = 1;
                if (parts[1].startsWith("[")) {
                    // пропускаем опции до следующего элемента
                    while (idx < parts.length && !parts[idx].startsWith("http://") && !parts[idx].startsWith("https://")) {
                        idx++;
                    }
                    if (idx >= parts.length) continue;
                }
                String uri = parts[idx++];
                if (idx >= parts.length) continue;
                String distribution = parts[idx++];
                List<String> components = new ArrayList<>();
                while (idx < parts.length) {
                    components.add(parts[idx++]);
                }

                AgentDataDto.RepositoryDto repo = new AgentDataDto.RepositoryDto();
                repo.setName(uri + " " + distribution);
                repo.setUrl(uri);
                repo.setDistribution(distribution);
                repo.setComponents(components);
                repos.add(repo);
            }
        } catch (IOException e) {
            log.error("Не удалось разобрать исходный файл apt: {}", file, e);
        }
    }

    // Парсер для .sources файлов (deb822)
    private void parseDeb822SourcesFile(Path file, List<AgentDataDto.RepositoryDto> repos) {
        try (Stream<String> lines = Files.lines(file)) {
            Map<String, List<String>> currentBlock = new LinkedHashMap<>();
            String currentKey = null;
            List<String> currentValues = new ArrayList<>();

            for (String line : (Iterable<String>) lines::iterator) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    if (currentKey != null) {
                        currentBlock.put(currentKey, currentValues);
                    }
                    if (!currentBlock.isEmpty()) {
                        processDeb822Block(currentBlock, repos);
                        currentBlock.clear();
                    }
                    currentKey = null;
                    currentValues = new ArrayList<>();
                    continue;
                }

                int colonIdx = line.indexOf(':');
                if (colonIdx > 0 && !line.startsWith(" ")) {
                    if (currentKey != null) {
                        currentBlock.put(currentKey, currentValues);
                    }
                    currentKey = line.substring(0, colonIdx).trim();
                    String value = line.substring(colonIdx + 1).trim();
                    currentValues = new ArrayList<>();
                    if (!value.isEmpty()) {
                        currentValues.addAll(Arrays.asList(value.split("\\s+")));
                    }
                } else {
                    if (currentKey != null && !line.isEmpty()) {
                        currentValues.addAll(Arrays.asList(line.split("\\s+")));
                    }
                }
            }
            if (currentKey != null) {
                currentBlock.put(currentKey, currentValues);
            }
            if (!currentBlock.isEmpty()) {
                processDeb822Block(currentBlock, repos);
            }
        } catch (IOException e) {
            log.error("Не удалось разобрать исходный файл deb822: {}", file, e);
        }
    }

    private void processDeb822Block(Map<String, List<String>> block, List<AgentDataDto.RepositoryDto> repos) {
        List<String> types = block.get("Types");
        List<String> uris = block.get("URIs");
        List<String> suites = block.get("Suites");
        List<String> components = block.getOrDefault("Components", new ArrayList<>());

        if (types == null || uris == null || suites == null) {
            return;
        }
        for (String uri : uris) {
            for (String suite : suites) {
                for (String type : types) {
                    if (!"deb".equals(type)) continue;
                    AgentDataDto.RepositoryDto repo = new AgentDataDto.RepositoryDto();
                    repo.setName(uri + " " + suite);
                    repo.setUrl(uri);
                    repo.setDistribution(suite);
                    repo.setComponents(components);
                    repos.add(repo);
                }
            }
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
                    module.setVersion(version != null ? version : "неизвестна");
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