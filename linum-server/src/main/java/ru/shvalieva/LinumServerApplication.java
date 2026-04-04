package ru.shvalieva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LinumServerApplication {
    public static void main(String[] args) {

        SpringApplication.run(LinumServerApplication.class, args);
    }
}