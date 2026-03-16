package ru.shvalieva.repository;

import ru.shvalieva.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ModuleRepository extends JpaRepository<Module, UUID> {
    Optional<Module> findByName(String name);
}