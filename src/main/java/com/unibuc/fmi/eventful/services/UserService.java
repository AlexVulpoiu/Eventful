package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.dto.UserDto;
import com.unibuc.fmi.eventful.dto.request.signup.AddModeratorDto;
import com.unibuc.fmi.eventful.exceptions.BadRequestException;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.model.Role;
import com.unibuc.fmi.eventful.model.User;
import com.unibuc.fmi.eventful.repository.AbstractUserRepository;
import com.unibuc.fmi.eventful.repository.RoleRepository;
import com.unibuc.fmi.eventful.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserService {

    @Value("${eventful.app.defaultPassword}")
    String defaultPassword;

    final AbstractUserRepository abstractUserRepository;
    final RoleRepository roleRepository;
    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;
    final SendEmailService sendEmailService;

    public List<UserDto> getAllUsers(String role) {
        role = role.toUpperCase();
        if (roleRepository.findByName(role).isEmpty()) {
            throw new NotFoundException("Role with name " + role + " not found!");
        }

        String finalRole = role;
        var users = userRepository.findAll().stream().filter(u -> u.getRoles().stream().map(Role::getName).toList().contains(finalRole));
        List<UserDto> userDtoList = new ArrayList<>();
        users.forEach(u -> userDtoList.add(new UserDto(u.getId(), u.getFullName(), u.getEmail(), u.isEnabled())));
        return userDtoList;
    }

    public void addModerator(AddModeratorDto addModeratorDto) throws MessagingException, UnsupportedEncodingException {
        if (abstractUserRepository.existsByEmail(addModeratorDto.getEmail())) {
            throw new BadRequestException("Email already in use!");
        }

        User user = new User(addModeratorDto.getFirstName(), addModeratorDto.getLastName(), addModeratorDto.getEmail(),
                passwordEncoder.encode(defaultPassword));
        Role moderatorRole = roleRepository.findByName("MODERATOR")
                .orElseThrow(() -> new NotFoundException("Moderator role not found!"));

        log.info("Adding moderator " + addModeratorDto.getEmail())
        Set<Role> roles = new HashSet<>();
        roles.add(moderatorRole);
        user.setRoles(roles);
        userRepository.save(user);

        sendEmailService.sendVerificationEmail(user);
    }

    public void changeRoleForUser(Long userId, String roleName) {
        var user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User with id " + userId + " not found!'"));
        roleName = roleName.toUpperCase();
        var role = roleRepository.findByName(roleName);
        if (role.isEmpty()) {
            throw new NotFoundException("Role with name " + roleName + " not found!");
        }

        log.info("Changing role for user " + userId + " to " + roleName);
        Set<Role> roles = new HashSet<>();
        roles.add(role.get());
        user.setRoles(roles);
        userRepository.save(user);
    }

    public void deleteModerator(Long userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User with id " + userId + " not found!'"));
        var role = roleRepository.findByName("MODERATOR");
        if (role.isEmpty()) {
            throw new NotFoundException("Role with name MODERATOR not found!");
        }
        log.info("Deleting moderator " + user.getEmail());
        user.setRoles(new HashSet<>());
        user = userRepository.save(user);

        userRepository.delete(user);
    }
}
