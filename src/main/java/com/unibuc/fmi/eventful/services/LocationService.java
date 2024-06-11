package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.dto.LocationDetailsDto;
import com.unibuc.fmi.eventful.dto.SeatedLocationDto;
import com.unibuc.fmi.eventful.dto.StandingLocationDto;
import com.unibuc.fmi.eventful.dto.request.location.AddSeatedLocationDto;
import com.unibuc.fmi.eventful.dto.request.location.AddStandingLocationDto;
import com.unibuc.fmi.eventful.mappers.LocationMapper;
import com.unibuc.fmi.eventful.model.AbstractLocation;
import com.unibuc.fmi.eventful.model.SeatedLocation;
import com.unibuc.fmi.eventful.model.SeatsCategory;
import com.unibuc.fmi.eventful.model.StandingLocation;
import com.unibuc.fmi.eventful.repository.AbstractLocationRepository;
import com.unibuc.fmi.eventful.repository.SeatedLocationRepository;
import com.unibuc.fmi.eventful.repository.StandingLocationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationService {

    LocationMapper locationMapper;
    SeatedLocationRepository seatedLocationRepository;
    StandingLocationRepository standingLocationRepository;
    AbstractLocationRepository abstractLocationRepository;

    public Long addStandingLocation(AddStandingLocationDto addStandingLocationDto) {
        var standingLocation = locationMapper.addStandingLocationDtoToStandingLocation(addStandingLocationDto);
        return standingLocationRepository.save(standingLocation).getId();
    }

    public Long addSeatedLocation(AddSeatedLocationDto addSeatedLocationDto) {
        var seatedLocation = locationMapper.addSeatedLocationDtoToSeatedLocation(addSeatedLocationDto);
        seatedLocation = seatedLocationRepository.save(seatedLocation);
        for (var sc : seatedLocation.getSeatsCategories()) {
            sc.setSeatedLocation(seatedLocation);
        }
        return seatedLocationRepository.save(seatedLocation).getId();
    }

    public List<StandingLocationDto> getStandingLocations(String search) {
        List<StandingLocation> standingLocations = Optional.ofNullable(search).isPresent()
                ? standingLocationRepository.getStandingLocations(search) : standingLocationRepository.findAll();

        return standingLocations.stream().map(locationMapper::standingLocationToDto).toList();
    }

    public List<SeatedLocationDto> getSeatedLocations(String search) {
        List<SeatedLocation> seatedLocations = Optional.ofNullable(search).isPresent()
                ? seatedLocationRepository.getSeatedLocations(search) : seatedLocationRepository.findAll();

        return seatedLocations.stream().map(locationMapper::seatedLocationToDto).toList();
    }

    public List<LocationDetailsDto> getLocations(String search) {
        if (search == null || search.isBlank()) {
            search = "";
        }
        List<AbstractLocation> locations = abstractLocationRepository.findAllByName(search);

        List<LocationDetailsDto> locationDetails = new ArrayList<>();
        for (var location : locations) {
            if (location instanceof SeatedLocation seatedLocation) {
                locationDetails.add(new LocationDetailsDto(seatedLocation.getId(), seatedLocation.getShortAddressWithName(),
                        seatedLocation.getSeatsCategories().stream().map(SeatsCategory::getName).toList(),
                        seatedLocation.getSeatsCategories().stream().map(SeatsCategory::getId).toList(),
                        seatedLocation.getNumberOfRows() * seatedLocation.getSeatsPerRow()));
            } else if (location instanceof StandingLocation standingLocation) {
                locationDetails.add(new LocationDetailsDto(standingLocation.getId(), standingLocation.getShortAddressWithName(),
                        new ArrayList<>(), new ArrayList<>(), standingLocation.getCapacity()));
            }
        }

        return locationDetails;
    }
}
