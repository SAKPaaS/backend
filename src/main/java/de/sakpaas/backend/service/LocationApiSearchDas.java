package de.sakpaas.backend.service;

import static java.util.Collections.emptyList;

import de.sakpaas.backend.dto.OsmResultLocationListDto;
import de.sakpaas.backend.util.OsmImportConfiguration;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class LocationApiSearchDas {

  private final RestTemplate restTemplate;

  public LocationApiSearchDas(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  /**
   * Builds the query url to the overpass-api from data of application.yaml
   *
   * @param config ImportConfig from application.yaml
   * @return Url as string
   */
  public String queryUrlBuilder(OsmImportConfiguration config) {
    // build request string
    StringBuilder url =
        new StringBuilder("https://overpass-api.de/api/interpreter?data=[out:json][timeout:2500];")
            .append("area[\"ISO3166-1:alpha2\"=").append(config.getCountry())
            .append("]->.searchArea;(");

    // Add shoptypes from configuration
    for (String shoptype : config.getShoptypes()) {
      url.append("node[shop=").append(shoptype).append("](area.searchArea);way[shop=")
          .append(shoptype).append("](area.searchArea);");
    }

    url.append(");out center;");

    return url.toString();
  }


  /**
   * Gets all Locations of specific types in a specific Country.
   *
   * @param osmImportConfiguration Object which contains all information about the data to load
   * @return list of supermarkets in Country
   */
  public List<OsmResultLocationListDto.OsmResultLocationDto> getLocationsForCountry(
      OsmImportConfiguration osmImportConfiguration) {

    // If no location types specified, there is nothing to load
    if (osmImportConfiguration.getShoptypes().isEmpty()) {
      return emptyList();
    }

    //build queryString
    String url = queryUrlBuilder(osmImportConfiguration);

    // make request
    ResponseEntity<OsmResultLocationListDto> response =
        restTemplate.getForEntity(url, OsmResultLocationListDto.class);

    if (response.getBody() == null) {
      return emptyList();
    }

    return response.getBody().getElements();
  }

}
