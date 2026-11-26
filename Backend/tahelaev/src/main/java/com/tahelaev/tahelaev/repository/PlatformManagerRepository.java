package com.tahelaev.tahelaev.repository;

import com.tahelaev.tahelaev.domain.PlatformManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformManagerRepository extends JpaRepository<PlatformManager, UUID> {
    Optional<PlatformManager> findByEmail(String email);
}
