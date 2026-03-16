package ru.shvalieva.repository;

import ru.shvalieva.model.RepoPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RepoPackageRepository extends JpaRepository<RepoPackage, UUID> {
}