package ru.shvalieva.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.shvalieva.model.PackageEntity;
import java.util.Optional;
import java.util.UUID;

public interface PackageEntityRepository extends JpaRepository<PackageEntity, UUID> {
    Optional<PackageEntity> findByName(String name);
}