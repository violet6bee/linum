package ru.shvalieva.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "host_modules")
@Data
public class HostModule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private Host host;

    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    private ModuleEntity moduleEntity;

    private String version;
    private Instant updatedAt;
}