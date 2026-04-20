package ru.shvalieva.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.shvalieva.dto.AgentDataDto;
import ru.shvalieva.service.CacheService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSender {

    private final ObjectMapper objectMapper;
    private final CacheService cacheService;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public boolean sendData(String serverUrl, AgentDataDto data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            log.debug("Отправка данных в {}: {}", serverUrl, json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + data.getToken())
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                log.info("Данные отправлены успешно, ответ: {}", response.body());
                return true;
            } else {
                log.error("Не удалось отправить данные, статус: {}, ответ: {}", response.statusCode(), response.body());
                cacheService.saveToCache(data);
                return false;
            }
        } catch (Exception e) {
            log.error("Ошибка при отправке данных", e);
            cacheService.saveToCache(data);
            return false;
        }
    }

    public boolean sendCachedData(String serverUrl, java.io.File cacheFile) {
        AgentDataDto data = cacheService.loadFromCache(cacheFile);
        if (data == null) {
            cacheService.deleteCacheFile(cacheFile);
            return false;
        }
        boolean success = sendData(serverUrl, data);
        if (success) {
            cacheService.deleteCacheFile(cacheFile);
        }
        return success;
    }
}