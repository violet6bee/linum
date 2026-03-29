package ru.shvalieva.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hosts")
@Data
public class Host {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String token;

    private String name;
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String osInfo;

    private String kernelVersion;
    private String architecture;
    private Instant firstSeen;
    private Instant lastUpdated;
}