package ru.shvalieva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.shvalieva.config.AgentProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AgentProperties.class)
public class LinumAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(LinumAgentApplication.class, args);
    }
}