package de.sakpaas.backend.service;

import de.sakpaas.backend.model.Presence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresenceRepository extends JpaRepository<Presence, Long> {
}
