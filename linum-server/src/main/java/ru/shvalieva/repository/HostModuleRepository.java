package ru.shvalieva.repository;

import ru.shvalieva.model.Host;
import ru.shvalieva.model.HostModule;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.shvalieva.model.ModuleEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HostModuleRepository extends JpaRepository<HostModule, UUID> {
    void deleteByHostId(UUID hostId);
    List<HostModule> findByHost(Host host);
    Optional<HostModule> findByHostAndModuleEntity(Host host, ModuleEntity moduleEntity);

}