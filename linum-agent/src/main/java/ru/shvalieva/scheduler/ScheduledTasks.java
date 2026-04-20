package ru.shvalieva.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.shvalieva.client.DataSender;
import ru.shvalieva.collector.SystemCollector;
import ru.shvalieva.config.AgentProperties;
import ru.shvalieva.dto.AgentDataDto;
import ru.shvalieva.service.CacheService;

import java.io.File;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final SystemCollector systemCollector;
    private final DataSender dataSender;
    private final AgentProperties properties;
    private final CacheService cacheService;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("Агент запущен. Проверка наличия кэшированных данных...");
        sendAllCachedData();
    }

    @Scheduled(fixedDelayString = "${agent.interval-ms}")
    public void collectAndSend() {
        log.info("Начало сбора данных для хоста: {}", properties.getHostId());
        AgentDataDto data = systemCollector.collect(properties.getHostId(), properties.getToken());
        log.debug("Собранные данные: {}", data);

        boolean success = dataSender.sendData(properties.getServerUrl(), data);
        if (success) {
            log.info("Данные отправлены успешно");
            sendAllCachedData();
        } else {
            log.error("Не удалось отправить данные, они сохранены в кэш. Повторная попытка при следующем запуске.");
        }
    }

    @Scheduled(fixedDelay = 3600000)
    public void retryCachedData() {
        if (cacheService.getCachedFiles().isEmpty()) {
            return;
        }
        log.info("Попытка отправить кэшированные данные...");
        sendAllCachedData();
    }

    private void sendAllCachedData() {
        for (File cacheFile : cacheService.getCachedFiles()) {
            boolean success = dataSender.sendCachedData(properties.getServerUrl(), cacheFile);
            if (success) {
                log.info("Кэш-файл отправлен: {}", cacheFile.getName());
            } else {
                log.warn("Не удалось отправить кэш-файл: {}, будет повторная попытка позже", cacheFile.getName());
            }
        }
    }
}