package de.sakpaas.backend.v2.controller;

import de.sakpaas.backend.BackendApplication;
import de.sakpaas.backend.model.Location;
import de.sakpaas.backend.model.Occupancy;
import de.sakpaas.backend.service.LocationService;
import de.sakpaas.backend.service.OccupancyService;
import de.sakpaas.backend.service.PresenceService;
import de.sakpaas.backend.v2.dto.LocationResultLocationDto;
import de.sakpaas.backend.v2.dto.OccupancyReportDto;
import de.sakpaas.backend.v2.mapper.LocationMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@CrossOrigin(origins = "*")
@RequestMapping("/v2/locations")
@RestController
public class LocationController {
    private static final String MAPPING_POST_OCCUPANCY = "/{locationId}/occupancy";
    private static final String MAPPING_POST_CHECKIN = "/{locationId}/check-in";
    private static final String MAPPING_BY_ID = "/{locationId}";
    private static final String MAPPING_START_DATABASE = "/generate/{key}";
    private LocationService locationService;
    private LocationMapper locationMapper;
    private OccupancyService occupancyService;
    private PresenceService presenceService;
    private AtomicBoolean importState;

    public LocationController(LocationService locationService,
                              LocationMapper locationMapper, OccupancyService occupancyService, PresenceService presenceService) {
        this.locationService = locationService;
        this.locationMapper = locationMapper;
        this.occupancyService = occupancyService;
        this.presenceService = presenceService;
        this.importState = new AtomicBoolean(false);
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<LocationResultLocationDto>> getLocation(@RequestParam Double latitude,
                                                                       @RequestParam Double longitude) {
        List<Location> searchResult = locationService.findByCoordinates(latitude, longitude);

        if (searchResult.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), OK);
        }

        List<LocationResultLocationDto> response = searchResult.stream()
                .map(locationMapper::mapToOutputDto)
                .collect(toList());

        return new ResponseEntity<>(response, OK);
    }

    @GetMapping(value = MAPPING_BY_ID)
    public ResponseEntity<LocationResultLocationDto> getById(@PathVariable("locationId") Long locationId) {
        Location location = locationService.getById(locationId).orElse(null);

        if (location == null) {
            return ResponseEntity.notFound().build();
        }

        return new ResponseEntity<>(locationMapper.mapToOutputDto(location), OK);
    }

    @PostMapping(value = MAPPING_POST_OCCUPANCY)
    public ResponseEntity<LocationResultLocationDto> postNewOccupancy(@RequestBody OccupancyReportDto occupancyReportDto,
                                                                      @PathVariable("locationId") Long locationId) {
        occupancyReportDto.setLocationId(locationId);
        Location location = locationService.getById(locationId).orElse(null);

        if (location == null) {
            return ResponseEntity.notFound().build();
        }

        occupancyService.save(new Occupancy(location, occupancyReportDto.getOccupancy(), occupancyReportDto.getClientType()));

        return new ResponseEntity<>(locationMapper.mapToOutputDto(location), CREATED);
    }

    @PostMapping(value = MAPPING_POST_CHECKIN)
    public ResponseEntity<String> postNewCheckIn(@PathVariable("locationId") Long locationId) {
        Location location = locationService.getById(locationId).orElse(null);

        if (location != null) {
            presenceService.addNewCheckin(location);
            return ResponseEntity.status(CREATED).build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = MAPPING_START_DATABASE)
    public ResponseEntity<String> startDatabase(@PathVariable("key") String key) {
        // Check key
        if (!key.equals(BackendApplication.GENERATED)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Permission denied");
        }

        // Check if it is the only query running
        if (importState.get()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Already running");
        }

        // Lock database import
        importState.set(true);
        // Making the Database import
        locationService.updateDatabase();
        // Unlock database import
        importState.set(false);

        return ResponseEntity.ok("Success");
    }
}
