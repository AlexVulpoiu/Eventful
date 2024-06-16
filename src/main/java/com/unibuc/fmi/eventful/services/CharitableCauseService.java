package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.dto.request.charitablecause.AddOrEditCharitableCauseDto;
import com.unibuc.fmi.eventful.exceptions.ForbiddenException;
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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CharitableCauseService {

    CharitableCauseRepository charitableCauseRepository;
    OrganiserRepository organiserRepository;
    CharitableCauseMapper charitableCauseMapper;

    public CharitableCause addCharitableCause(AddOrEditCharitableCauseDto addCharitableCauseDto, Long organiserId) {
        Organiser organiser = organiserRepository.findById(organiserId)
                .orElseThrow(() -> new NotFoundException("Organiser with id " + organiserId + " not found!"));
        var charitableCause = charitableCauseMapper.addCharitableCauseDtoToCharitableCause(addCharitableCauseDto);
        charitableCause.setOrganiser(organiser);
        charitableCause = charitableCauseRepository.save(charitableCause);
        return charitableCause;
    }

    public CharitableCause editCharitableCause(Long causeId, AddOrEditCharitableCauseDto editCharitableCauseDto, Long organiserId) {
        var charitableCause = charitableCauseRepository.findById(causeId)
                .orElseThrow(() -> new NotFoundException("Charitable cause with id " + causeId + " not found!"));
        organiserRepository.findById(organiserId)
                .orElseThrow(() -> new NotFoundException("Organiser with id " + organiserId + " not found!"));
        if (!charitableCause.getOrganiser().getId().equals(organiserId)) {
            throw new ForbiddenException("You are not allowed to perform this action!");
        }

        charitableCause.setName(editCharitableCauseDto.getName());
        charitableCause.setDescription(editCharitableCauseDto.getDescription());
        charitableCause.setNeededAmount(editCharitableCauseDto.getNeededAmount());
        charitableCause.setUpdatedAt(LocalDateTime.now());

        return charitableCauseRepository.save(charitableCause);
    }

    public void updateCollectedAmount(CharitableCause charitableCause, double amount) {
        var collectedAmount = charitableCause.getCollectedAmount();
        charitableCause.setCollectedAmount(collectedAmount + amount);
        charitableCauseRepository.save(charitableCause);
    }
}
