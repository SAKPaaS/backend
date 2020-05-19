package de.sakpaas.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "OCCUPANCY_HISTORY")
@IdClass(OccupancyHistoryId.class)
public class OccupancyHistory {

  @Id
  @ManyToOne(optional = false)
  @JoinColumn(name = "LOCATION_ID", referencedColumnName = "ID", nullable = false)
  private Location location;

  @Id
  @Column(name = "AGGREGATION_HOUR", nullable = false)
  @Range(min = 0, max = 168)
  private int aggregationHour;

  @Column(name = "OCCUPANCY_SUM")
  private double occupancySum;

  @Column(name = "OCCUPANCY_COUNT")
  private int occupancyCount;

  public OccupancyHistory(Location location, int aggregationHour) {
    this.location = location;
    this.aggregationHour = aggregationHour;
    this.occupancySum = 0.0;
    this.occupancyCount = 0;
  }

  public OccupancyHistory(Location location, int aggregationHour, double occupancySum,
                          int occupancyCount) {
    this.location = location;
    this.aggregationHour = aggregationHour;
    this.occupancySum = occupancySum;
    this.occupancyCount = occupancyCount;
  }

  public void increment(double occupancy) {
    this.occupancySum += occupancy;
    this.occupancyCount++;
  }
}
