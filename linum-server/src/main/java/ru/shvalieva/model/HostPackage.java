package ru.shvalieva.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "host_packages")
@Data
public class HostPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private Host host;

    @ManyToOne
    @JoinColumn(name = "package_id", nullable = false)
    private PackageEntity packageEntity;

    private String version;
    private String architecture;
    private Instant updatedAt;
}