package ru.shvalieva.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.shvalieva.model.Host;
import ru.shvalieva.model.HostUpgradable;

import java.util.List;
import java.util.UUID;

public interface HostUpgradableRepository extends JpaRepository<HostUpgradable, UUID> {
    List<HostUpgradable> findByHost(Host host);
    void deleteByHost(Host host);
}