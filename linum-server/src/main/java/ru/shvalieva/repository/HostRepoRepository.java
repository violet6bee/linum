package ru.shvalieva.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.shvalieva.model.Host;
import ru.shvalieva.model.HostRepo;
import java.util.List;
import java.util.UUID;

public interface HostRepoRepository extends JpaRepository<HostRepo, UUID> {
    List<HostRepo> findByHost(Host host);
}