package de.sakpaas.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "LOCATION_DETAILS")
public class LocationDetails {

  @Id
  @GeneratedValue
  @Column(name = "ID", nullable = false)
  private Long id;

  @Column(name = "TYPE")
  private String type;

  @Column(name = "OPENING_HOURS")
  private String openingHours;

  @Column(name = "BRAND")
  private String brand;

  /**
   * Constructor for creating a new LocationDetails Entity without id.
   *
   * @param type         the type (eg. supermarket)
   * @param openingHours the openingHours in human readable format
   * @param brand        the brand (eg. LIDL, ALDI, ...)
   */
  public LocationDetails(String type, String openingHours, String brand) {
    this.type = type;
    this.openingHours = openingHours;
    this.brand = brand;
  }
}
