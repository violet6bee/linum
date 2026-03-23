package ru.shvalieva.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.shvalieva.client.DataSender;
import ru.shvalieva.collector.SystemCollector;
import ru.shvalieva.config.AgentProperties;
import ru.shvalieva.dto.AgentDataDto;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final SystemCollector systemCollector;
    private final DataSender dataSender;
    private final AgentProperties properties;

    @Scheduled(fixedDelayString = "#{@agentProperties.intervalSeconds * 1000}")
    public void collectAndSend() {
        log.info("Начало сбора данных для хоста: {}", properties.getHostId());
        AgentDataDto data = systemCollector.collect(properties.getHostId(), properties.getToken());
        log.debug("Собранные данные: {}", data);

        boolean success = dataSender.sendData(properties.getServerUrl(), data);
        if (success) {
            log.info("Данные отправлены успешно");
        } else {
            log.error("Не удалось отправить данные, повторите попытку при следующем запланированном запуске");
        }
    }
}