package de.sakpaas.backend.v2.mapper;

import de.sakpaas.backend.model.Location;
import de.sakpaas.backend.service.OccupancyService;
import de.sakpaas.backend.v2.dto.LocationResultLocationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

  private final OccupancyService occupancyService;

  /**
   * Maps a Location to a LocationResultLocationDto.
   *
   * @param occupancyService The occupancy service
   */
  @Autowired
  public LocationMapper(OccupancyService occupancyService) {
    this.occupancyService = occupancyService;
  }

  /**
   * Maps the given Location to a v2 LocationDto.
   *
   * @param location the Location to be mapped
   * @return the mapped LocationDto
   */
  public LocationResultLocationDto mapLocationToOutputDto(Location location) {
    if (location == null) {
      return null;
    }

    return new LocationResultLocationDto(
        location.getId(), location.getName(),
        new LocationResultLocationDto.LocationResultLocationDetailsDto(location.getDetails()),
        new LocationResultLocationDto.LocationResultCoordinatesDto(location.getLatitude(),
            location.getLongitude()),
        new LocationResultLocationDto.LocationResultOccupancyDto(
            occupancyService.getOccupancyCalculation(location)),
        new LocationResultLocationDto.LocationResultAddressDto(location.getAddress()));
  }
}
