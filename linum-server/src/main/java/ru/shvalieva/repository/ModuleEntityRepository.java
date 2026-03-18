package ru.shvalieva.repository;

import ru.shvalieva.model.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ModuleEntityRepository extends JpaRepository<ModuleEntity, UUID> {
    Optional<ModuleEntity> findByName(String name);
}