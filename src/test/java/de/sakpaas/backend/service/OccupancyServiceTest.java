package de.sakpaas.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;


import de.sakpaas.backend.HappyHamsterTest;
import de.sakpaas.backend.exception.TooManyRequestsException;
import de.sakpaas.backend.model.AccumulatedOccupancy;
import de.sakpaas.backend.model.Location;
import de.sakpaas.backend.model.Occupancy;
import de.sakpaas.backend.util.OccupancyAccumulationConfiguration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
class OccupancyServiceTest extends HappyHamsterTest {

  private final static int TEST_DURATION_HOURS = 12;

  @Autowired
  OccupancyService occupancyService;
  @Autowired
  OccupancyAccumulationConfiguration config;

  @MockBean
  OccupancyRepository occupancyRepository;

  @Test
  void testGetOccupancyCalculationOneOccupancy() {
    Location location = new Location();
    ZonedDateTime timestamp = ZonedDateTime.of(2020, 1, 1, 1, 1, 1, 1, ZoneId.of("Z"));

    // Test for TEST_DURATION_HOURS hours that occupancy will be equals when only one is entered
    for (int x = 0; x < TimeUnit.HOURS.toMinutes(TEST_DURATION_HOURS); x++) {
      // Create test data
      double reportedOccupancy = 0.5;
      ZonedDateTime offsetTimestamp = timestamp.minusMinutes(x);
      Occupancy occupancy = makeOccupancy(1L, location, reportedOccupancy, offsetTimestamp, "TEST");

      // Mock test data
      Mockito.when(
          occupancyRepository.findByLocationAndTimestampAfter(Mockito.eq(location), any())
      ).thenReturn(Collections.singletonList(occupancy));

      // Run calculation
      AccumulatedOccupancy accumulatedOccupancy =
          occupancyService.getOccupancyCalculation(location);

      // Test result
      assertEquals(reportedOccupancy, accumulatedOccupancy.getValue(),
          "The reported occupancy should be equal to the calculated occupancy.");
      assertEquals(1, accumulatedOccupancy.getCount(),
          "The count of the occupancy reports should be 1.");
      assertEquals(offsetTimestamp, accumulatedOccupancy.getLatestReport(),
          "The latest report should be the time of the only report.");
    }
  }

  @Test
  void testGetOccupancyCalculationDecreasingFactor() {
    Location location = new Location();
    ZonedDateTime timestamp = ZonedDateTime.of(2020, 1, 1, 1, 1, 1, 1, ZoneId.of("Z"));

    // Test for TEST_DURATION_HOURS hours that the most recent occupancy report will
    // be more valuable than the others
    for (int x = 0; x < TimeUnit.HOURS.toMinutes(TEST_DURATION_HOURS); x++) {
      // Create test data
      double reportedOccupancyMin = 0.0;
      double reportedOccupancyMax = 1.0;
      double reportedOccupancyAvg = (reportedOccupancyMin + reportedOccupancyMax) / 2.0;
      ZonedDateTime offsetTimestampMin = timestamp.minusMinutes(x + 1);
      ZonedDateTime offsetTimestampMax = timestamp.minusMinutes(x);
      Occupancy occupancyMin =
          makeOccupancy(1L, location, reportedOccupancyMin, offsetTimestampMin, "TEST_MIN");
      Occupancy occupancyMax =
          makeOccupancy(1L, location, reportedOccupancyMax, offsetTimestampMax, "TEST_MAX");

      // Mock test data
      Mockito.when(
          occupancyRepository.findByLocationAndTimestampAfter(Mockito.eq(location), any())
      ).thenReturn(
          Arrays.asList(occupancyMin, occupancyMax)
      );

      // Run calculation
      AccumulatedOccupancy accumulatedOccupancy =
          occupancyService.getOccupancyCalculation(location);

      // Test result
      assertTrue(accumulatedOccupancy.getValue() >= reportedOccupancyAvg,
          "The reported occupancy should be always greater or equal to the average.");
      assertEquals(2, accumulatedOccupancy.getCount(),
          "The count of the occupancy reports should be 1.");
      assertEquals(offsetTimestampMax, accumulatedOccupancy.getLatestReport(),
          "The latest report should be the time of the latest occupancy.");
    }
  }

  @Test
  void testCalculateFactorConstant() {
    // Test all minutes where the factor should be constant 1.0
    for (int x = 0; x <= config.getConstant(); x++) {
      // Use -x as we are progressing backwards in time
      assertEquals(1.0, occupancyService.calculateAccumulationFactor(-x),
          "The occupancy should stay at 1.0 for constant time.");
    }
  }

  @Test
  void testCalculateFactorMonotonicallyDecreasing() {
    // Test for TEST_DURATION_HOURS hours that the factor is monotonically decreasing
    double before = Double.MAX_VALUE;
    for (int x = 0; x < TimeUnit.HOURS.toMinutes(TEST_DURATION_HOURS); x++) {
      // Use -x as we are progressing backwards in time
      double value = occupancyService.calculateAccumulationFactor(-x);
      assertTrue(value <= before,
          "The factor should be decreasing over time (or staying at the same level).");
      // Set new before
      before = value;
    }
  }

  @Test
  void testCalculateFactorMaximumOne() {
    // Test for TEST_DURATION_HOURS hours that the factor is at most 1.0
    for (int x = 0; x < TimeUnit.HOURS.toMinutes(TEST_DURATION_HOURS); x++) {
      // Use -x as we are progressing backwards in time
      double value = occupancyService.calculateAccumulationFactor(-x);
      assertTrue(value <= 1.0,
          "The factor should never be exceed 1.0.");
    }
  }

  @Test
  void testCalculateFactorMinimum() {
    // Test for TEST_DURATION_HOURS hours that the factor is always at least "minimum"
    for (int x = 0; x < TimeUnit.HOURS.toMinutes(TEST_DURATION_HOURS); x++) {
      // Use -x as we are progressing backwards in time
      double value = occupancyService.calculateAccumulationFactor(-x);
      assertTrue(value >= config.getMinimum(),
          "The factor should never be smaller than the minimum.");
    }
  }

  @Test
  void testCalculateFactorBounds() {
    // Test the bounds
    // Upper bound
    assertEquals(1.0, occupancyService.calculateAccumulationFactor(0),
        "The factor should be 1.0 at the beginning.");
    // Lower bound
    // Use -x as we are progressing backwards in time
    double value = occupancyService
        .calculateAccumulationFactor(-TimeUnit.HOURS.toMinutes(TEST_DURATION_HOURS));
    assertTrue(value <= (1.05) * config.getMinimum(),
        "After some time, the value should be near the minimum (5% margin).");
  }

  @Test
  void testSave() {
    // Create test data
    Location location = new Location();
    ZonedDateTime timestamp = ZonedDateTime.of(2020, 1, 1, 1, 1, 1, 1, ZoneId.of("Z"));
    Occupancy occupancy = makeOccupancy(1L, location, 0.0, timestamp, "TEST");

    // Mock test data
    Mockito.when(occupancyRepository.save(occupancy)).thenReturn(occupancy);

    // Test
    // (This test is really meaningless...)
    assertSame(occupancy, occupancyService.save(occupancy),
        "The saved occupancy should be the same occupancy returned.");
  }

  @Test
  void testCheckReportLimitUser() {
    Location location = new Location();
    UUID uuid = UUID.fromString("550e8400-e29b-11d4-a716-446655440000");
    List<Occupancy> list0 = new ArrayList<>();
    List<Occupancy> list5 = Arrays.asList(
        makeOccupancy(1L, location, 0.5, null, ""),
        makeOccupancy(2L, location, 0.5, null, ""),
        makeOccupancy(3L, location, 0.5, null, ""),
        makeOccupancy(4L, location, 0.5, null, ""),
        makeOccupancy(5L, location, 0.5, null, "")
    );

    Mockito.when(occupancyRepository.findByLocationAndUserUuidAndTimestampAfter(any(), any(), any()))
        .thenReturn(list5);
    Mockito.when(occupancyRepository.findByUserUuidAndTimestampAfter(any(), any()))
        .thenReturn(list0);
    assertThrows(TooManyRequestsException.class, () -> occupancyService.checkReportLimit(location, uuid));

    Mockito.when(occupancyRepository.findByLocationAndUserUuidAndTimestampAfter(any(), any(), any()))
        .thenReturn(list0);
    Mockito.when(occupancyRepository.findByUserUuidAndTimestampAfter(any(), any()))
        .thenReturn(list5);
    assertThrows(TooManyRequestsException.class, () -> occupancyService.checkReportLimit(location, uuid));

    Mockito.when(occupancyRepository.findByLocationAndUserUuidAndTimestampAfter(any(), any(), any()))
        .thenReturn(list0);
    Mockito.when(occupancyRepository.findByUserUuidAndTimestampAfter(any(), any()))
        .thenReturn(list0);
    occupancyService.checkReportLimit(location, uuid);
  }
  @Test
  void testCheckReportLimitRequestHash() {
    Location location = new Location();
    byte[] requestHash = new byte[]{(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
    List<Occupancy> list0 = new ArrayList<>();
    List<Occupancy> list5 = Arrays.asList(
        makeOccupancy(1L, location, 0.5, null, ""),
        makeOccupancy(2L, location, 0.5, null, ""),
        makeOccupancy(3L, location, 0.5, null, ""),
        makeOccupancy(4L, location, 0.5, null, ""),
        makeOccupancy(5L, location, 0.5, null, "")
    );

    Mockito.when(occupancyRepository.findByLocationAndRequestHashAndTimestampAfter(any(), any(), any()))
        .thenReturn(list5);
    Mockito.when(occupancyRepository.findByRequestHashAndTimestampAfter(any(), any()))
        .thenReturn(list0);
    assertThrows(TooManyRequestsException.class, () -> occupancyService.checkReportLimit(location, requestHash));

    Mockito.when(occupancyRepository.findByLocationAndRequestHashAndTimestampAfter(any(), any(), any()))
        .thenReturn(list0);
    Mockito.when(occupancyRepository.findByRequestHashAndTimestampAfter(any(), any()))
        .thenReturn(list5);
    assertThrows(TooManyRequestsException.class, () -> occupancyService.checkReportLimit(location, requestHash));

    Mockito.when(occupancyRepository.findByLocationAndRequestHashAndTimestampAfter(any(), any(), any()))
        .thenReturn(list0);
    Mockito.when(occupancyRepository.findByRequestHashAndTimestampAfter(any(), any()))
        .thenReturn(list0);
    occupancyService.checkReportLimit(location, requestHash);
  }

  private Occupancy makeOccupancy(long id, Location location, double occupancy,
                                  ZonedDateTime timestamp, String clientType) {
    Occupancy object = new Occupancy();
    object.setId(id);
    object.setLocation(location);
    object.setOccupancy(occupancy);
    object.setTimestamp(timestamp);
    object.setClientType(clientType);
    return object;
  }
}