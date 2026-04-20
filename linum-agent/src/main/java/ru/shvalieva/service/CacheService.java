package ru.shvalieva.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.shvalieva.dto.AgentDataDto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final ObjectMapper objectMapper;
    private static final String CACHE_DIR = "agent_cache";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    public void saveToCache(AgentDataDto data) {
        try {
            Path cachePath = Paths.get(CACHE_DIR);
            if (!Files.exists(cachePath)) {
                Files.createDirectories(cachePath);
            }
            String timestamp = FORMATTER.format(Instant.now());
            String fileName = "cache_" + timestamp + ".json";
            Path filePath = cachePath.resolve(fileName);
            objectMapper.writeValue(filePath.toFile(), data);
            log.info("Данные сохранены в кэш: {}", filePath);
        } catch (IOException e) {
            log.error("Ошибка сохранения данных в кэш", e);
        }
    }

    public List<File> getCachedFiles() {
        List<File> files = new ArrayList<>();
        Path cachePath = Paths.get(CACHE_DIR);
        if (Files.exists(cachePath)) {
            try {
                Files.list(cachePath)
                        .filter(Files::isRegularFile)
                        .map(Path::toFile)
                        .forEach(files::add);
            } catch (IOException e) {
                log.error("Ошибка чтения кэш-директории", e);
            }
        }
        return files;
    }

    public AgentDataDto loadFromCache(File file) {
        try {
            return objectMapper.readValue(file, AgentDataDto.class);
        } catch (IOException e) {
            log.error("Ошибка загрузки кэш-файла: {}", file.getName(), e);
            return null;
        }
    }

    public void deleteCacheFile(File file) {
        if (file.exists() && file.delete()) {
            log.info("Кэш-файл удалён: {}", file.getName());
        } else {
            log.warn("Не удалось удалить кэш-файл: {}", file.getName());
        }
    }
}