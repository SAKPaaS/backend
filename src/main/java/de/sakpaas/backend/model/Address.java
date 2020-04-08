package de.sakpaas.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Data;

/**
 * @Data beinhaltet verschiedene Annotationen (z.B. @Getter, @Setter)
 * 
 *       Da diese Annotation auch die @RequiredArgsConstructor beinhaltet ist hier
 *       kein @NoArgsConstructor mehr nötig
 */
@Data
@Entity(name = "ADDRESS")
public class Address {

  @Id
  @GeneratedValue
  @Column(name = "ID", nullable = false)
  private Long id;

  @Column(name = "COUNTRY")
  private String country;

  @Column(name = "CITY")
  private String city;

  @Column(name = "POSTCODE")
  private String postcode;

  @Column(name = "STREET")
  private String street;

  @Column(name = "HOUSENUMBER")
  private String housenumber;

  /**
   * Creates an Address from scratch. Can be saved newly into the database.
   *
   * @param country country of the address
   * @param city city of the address
   * @param postcode postal code of the address
   * @param street street of the address
   * @param housenumber house number of the address
   */
  public Address(String country, String city, String postcode, String street, String housenumber) {
    this.country = country;
    this.city = city;
    this.postcode = postcode;
    this.street = street;
    this.housenumber = housenumber;
  }
}
