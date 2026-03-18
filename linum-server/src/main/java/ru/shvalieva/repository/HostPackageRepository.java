package ru.shvalieva.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.shvalieva.model.Host;
import ru.shvalieva.model.HostPackage;
import ru.shvalieva.model.PackageEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HostPackageRepository extends JpaRepository<HostPackage, UUID> {
    void deleteByHostId(UUID hostId);
    List<HostPackage> findByHost(Host host);
    Optional<HostPackage> findByHostAndPackageEntity(Host host, PackageEntity packageEntity);
}