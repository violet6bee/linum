package ru.shvalieva.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.shvalieva.model.HostPackage;
import java.util.UUID;

public interface HostPackageRepository extends JpaRepository<HostPackage, UUID> {
    void deleteByHostId(UUID hostId);
}