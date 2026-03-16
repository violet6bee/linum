package ru.shvalieva.repository;

import ru.shvalieva.model.HostModule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface HostModuleRepository extends JpaRepository<HostModule, UUID> {
    void deleteByHostId(UUID hostId);
}