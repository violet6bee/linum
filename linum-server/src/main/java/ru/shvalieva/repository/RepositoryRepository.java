package ru.shvalieva.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.shvalieva.model.Repository;
import java.util.Optional;
import java.util.UUID;

public interface RepositoryRepository extends JpaRepository<Repository, UUID> {
    Optional<Repository> findByUrl(String url);
}