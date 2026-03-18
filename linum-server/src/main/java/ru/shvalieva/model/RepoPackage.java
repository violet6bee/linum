package ru.shvalieva.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "repo_packages")
@Data
public class RepoPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    @ManyToOne
    @JoinColumn(name = "package_id", nullable = false)
    private PackageEntity aPackageEntity;

    @Column(nullable = false)
    private String latestVersion;

    @Column(nullable = false)
    private Instant updatedAt;
}