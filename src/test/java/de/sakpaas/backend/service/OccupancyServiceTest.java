package de.sakpaas.backend.service;

import de.sakpaas.backend.model.Location;
import de.sakpaas.backend.model.Occupancy;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ComponentScan
class OccupancyServiceTest {

  @Test
  void getAverageOccupancy() {
    Location location = new Location(1829487L, "LIDL", 42.0, 8.0);
    ZonedDateTime time = ZonedDateTime.now();

    List<Occupancy> occupancyList = new ArrayList<>();
    occupancyList.add(new Occupancy(12345L, location, 0.5, time.minusMinutes(15)));
    occupancyList.add(new Occupancy(12345L, location, 0.8, time.minusMinutes(30)));
    occupancyList.add(new Occupancy(12345L, location, 1.0, time.minusMinutes(45)));

    assertTrue(1.0 > OccupancyService.calculateAverage(occupancyList, time));
  }

  @Test
  void bellCurve() {
    assertTrue(0.95 < OccupancyService.bellCurve(-15));
    assertTrue(1.0 > OccupancyService.bellCurve(-15));

    assertTrue(0.0 < OccupancyService.bellCurve(-30));
    assertTrue(0.0 < OccupancyService.bellCurve(-45));
    assertTrue(0.0 < OccupancyService.bellCurve(-60));
    assertTrue(0.0 < OccupancyService.bellCurve(-105));
    assertTrue(0.0 < OccupancyService.bellCurve(-120));
  }
}