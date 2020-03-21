package de.sakpaas.backend.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@JsonPropertyOrder({ "locationId", "occupancy" })
@Getter
@Setter
public class OccupancyDto {
    private Long locationId;
    private Double occupancy;

    @JsonCreator
    public OccupancyDto(@JsonProperty("locationId") Long locationId,
            @JsonProperty("occupancy") Double occupancy) {
        this.locationId = locationId;
        this.occupancy = occupancy;
    }
}