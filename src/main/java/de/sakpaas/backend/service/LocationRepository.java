package de.sakpaas.backend.service;

import de.sakpaas.backend.model.Location;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationRepository extends JpaRepository<Location, Long> {
  Optional<Location> findById(Long id);

  List<Location> findByLatitudeBetweenAndLongitudeBetween(Double latMin, Double latMax,
                                                          Double lonMin, Double lonMax);

  @Query(value = "SELECT l.id FROM LOCATION l JOIN ADDRESS a ON l.address_id = a.id "
      + "WHERE a.country=(:country)", nativeQuery = true)
  List<Long> getAllIdsForCountry(@Param("country") String country);
}
