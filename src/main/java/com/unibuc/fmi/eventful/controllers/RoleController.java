package com.unibuc.fmi.eventful.controllers;

import com.unibuc.fmi.eventful.model.Role;
import com.unibuc.fmi.eventful.services.RoleService;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {

    RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Role> getRoles() {
        return roleService.getAllRoles();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public void addRole(@NotBlank @RequestBody String name) {
        roleService.addRole(name);
    }
}
