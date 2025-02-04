package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.exceptions.BadRequestException;
import com.unibuc.fmi.eventful.model.Role;
import com.unibuc.fmi.eventful.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {

    RoleRepository roleRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public void addRole(String roleName) {
        roleName = roleName.toUpperCase();
        if (roleRepository.findByName(roleName).isPresent()) {
            throw new BadRequestException("Role with name " + roleName + " already exists!");
        }

        log.info("Adding role with name " + roleName);
        roleRepository.save(Role.builder().name(roleName).build());
    }
}
