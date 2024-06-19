package com.unibuc.fmi.eventful.controllers;

import com.unibuc.fmi.eventful.dto.UserDto;
import com.unibuc.fmi.eventful.dto.request.signup.AddModeratorDto;
import com.unibuc.fmi.eventful.services.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserDto> getUsers(@RequestParam String role) {
        return userService.getAllUsers(role);
    }

    @PostMapping("/moderator")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void addModerator(@Valid @RequestBody AddModeratorDto addModeratorDto) throws MessagingException, UnsupportedEncodingException {
        userService.addModerator(addModeratorDto);
    }

    @PutMapping("/role/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void changeRoleForUser(@PathVariable Long userId, @NotBlank @RequestBody String role) {
        userService.changeRoleForUser(userId, role);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteModerator(@PathVariable Long userId) {
        userService.deleteModerator(userId);
    }
}
