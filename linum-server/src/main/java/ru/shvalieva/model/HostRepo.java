package ru.shvalieva.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "host_repositories")
@Data
public class HostRepo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private Host host;

    @ManyToOne
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    private Instant discoveredAt;
}