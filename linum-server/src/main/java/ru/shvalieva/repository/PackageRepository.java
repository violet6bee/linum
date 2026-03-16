package ru.shvalieva.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.shvalieva.model.Package;
import java.util.Optional;
import java.util.UUID;

public interface PackageRepository extends JpaRepository<Package, UUID> {
    Optional<Package> findByName(String name);
}