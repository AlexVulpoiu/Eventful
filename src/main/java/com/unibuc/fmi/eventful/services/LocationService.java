package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.dto.SeatedLocationDto;
import com.unibuc.fmi.eventful.dto.StandingLocationDto;
import com.unibuc.fmi.eventful.dto.request.location.AddSeatedLocationDto;
import com.unibuc.fmi.eventful.dto.request.location.AddStandingLocationDto;
import com.unibuc.fmi.eventful.mappers.LocationMapper;
import com.unibuc.fmi.eventful.model.SeatedLocation;
import com.unibuc.fmi.eventful.model.StandingLocation;
import com.unibuc.fmi.eventful.repository.SeatedLocationRepository;
import com.unibuc.fmi.eventful.repository.StandingLocationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationService {

    LocationMapper locationMapper;
    SeatedLocationRepository seatedLocationRepository;
    StandingLocationRepository standingLocationRepository;

    public void addStandingLocation(AddStandingLocationDto addStandingLocationDto) {
        var standingLocation = locationMapper.addStandingLocationDtoToStandingLocation(addStandingLocationDto);
        standingLocationRepository.save(standingLocation);
    }

    public void addSeatedLocation(AddSeatedLocationDto addSeatedLocationDto) {
        var seatedLocation = locationMapper.addSeatedLocationDtoToSeatedLocation(addSeatedLocationDto);
        seatedLocation = seatedLocationRepository.save(seatedLocation);
        for (var sc : seatedLocation.getSeatsCategories()) {
            sc.setSeatedLocation(seatedLocation);
        }
        seatedLocationRepository.save(seatedLocation);
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
}
