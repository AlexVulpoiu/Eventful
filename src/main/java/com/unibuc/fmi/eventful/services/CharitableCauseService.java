package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.dto.CharitableCauseDto;
import com.unibuc.fmi.eventful.dto.request.charitablecause.AddCharitableCauseDto;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.mappers.CharitableCauseMapper;
import com.unibuc.fmi.eventful.model.CharitableCause;
import com.unibuc.fmi.eventful.model.Organiser;
import com.unibuc.fmi.eventful.repository.CharitableCauseRepository;
import com.unibuc.fmi.eventful.repository.OrganiserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CharitableCauseService {

    CharitableCauseRepository charitableCauseRepository;
    OrganiserRepository organiserRepository;
    CharitableCauseMapper charitableCauseMapper;

    public CharitableCause addCharitableCause(AddCharitableCauseDto addCharitableCauseDto, Long organiserId) {
        Organiser organiser = organiserRepository.findById(organiserId)
                .orElseThrow(() -> new NotFoundException("Organiser with id " + organiserId + " not found!"));
        var charitableCause = charitableCauseMapper.addCharitableCauseDtoToCharitableCause(addCharitableCauseDto);
        charitableCause.setOrganiser(organiser);
        charitableCause = charitableCauseRepository.save(charitableCause);
        return charitableCause;
    }

    public List<CharitableCauseDto> getCharitableCausesForOrganiser(Long organiserId, String search) {
        Organiser organiser = organiserRepository.findById(organiserId)
                .orElseThrow(() -> new NotFoundException("Organiser with id " + organiserId + " not found!"));

        var charitableCauses = organiser.getCharitableCauses().stream();
        if (Optional.ofNullable(search).isPresent()) {
            charitableCauses = charitableCauses.filter((c -> c.getName().toLowerCase().contains(search.toLowerCase())));
        }

        return charitableCauses.sorted(Comparator.comparing(CharitableCause::getEndDate))
                .map(charitableCauseMapper::charitableCauseToCharitableCauseDto).toList();
    }
}
