package ru.shvalieva.repository;

import ru.shvalieva.model.PackageEntity;
import ru.shvalieva.model.RepoPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.shvalieva.model.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepoPackageRepository extends JpaRepository<RepoPackage, UUID> {
    Optional<RepoPackage> findByRepositoryAndPackageEntity(Repository repository, PackageEntity packageEntity);
    List<RepoPackage> findAllByPackageEntity(PackageEntity packageEntity);
}