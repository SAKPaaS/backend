package de.sakpaas.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @Data beinhaltet verschiedene Annotationen (z.B. @Getter, @Setter)
 *
 *       Da diese Annotation auch die @RequiredArgsConstructor beinhaltet ist hier
 *       @NoArgsConstructor noch notwendig, da Lombok nicht weiß, dass Spring den hier braucht
 */
@Data
@NoArgsConstructor
@Entity(name = "ADDRESS")
public class Address {

  @Id
  @GeneratedValue
  @Column(name = "ID", nullable = false)
  private Long id;

  @Column(name = "COUNTRY", length = 10)
  private String country;

  @Column(name = "CITY", length = 60)
  private String city;

  @Column(name = "POSTCODE", length = 10)
  private String postcode;

  @Column(name = "STREET", length = 75)
  private String street;

  @Column(name = "HOUSENUMBER", length = 50)
  private String housenumber;

  /**
   * Creates an Address from scratch. Can be saved newly into the database.
   *
   * @param country     country of the address
   * @param city        city of the address
   * @param postcode    postal code of the address
   * @param street      street of the address
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
