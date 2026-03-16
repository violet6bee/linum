package ru.shvalieva.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.shvalieva.model.HostRepository;
import java.util.UUID;

public interface HostRepositoryRepository extends JpaRepository<HostRepository, UUID> {
    void deleteByHostId(UUID hostId);
}