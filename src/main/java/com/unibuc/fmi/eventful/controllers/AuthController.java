package com.unibuc.fmi.eventful.controllers;

import com.unibuc.fmi.eventful.dto.request.signin.LoginRequest;
import com.unibuc.fmi.eventful.dto.request.signup.LegalPersonSignupRequest;
import com.unibuc.fmi.eventful.dto.request.signup.PersonSignupRequest;
import com.unibuc.fmi.eventful.dto.request.signup.UserSignupRequest;
import com.unibuc.fmi.eventful.dto.response.JwtResponse;
import com.unibuc.fmi.eventful.enums.OrganiserStatus;
import com.unibuc.fmi.eventful.exceptions.BadRequestException;
import com.unibuc.fmi.eventful.exceptions.NotFoundException;
import com.unibuc.fmi.eventful.mappers.OrganiserMapper;
import com.unibuc.fmi.eventful.model.AbstractUser;
import com.unibuc.fmi.eventful.model.Role;
import com.unibuc.fmi.eventful.model.User;
import com.unibuc.fmi.eventful.repository.*;
import com.unibuc.fmi.eventful.security.jwt.JwtUtils;
import com.unibuc.fmi.eventful.security.services.UserDetailsImpl;
import com.unibuc.fmi.eventful.services.SendEmailService;
import com.unibuc.fmi.eventful.services.TokenBlacklistService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthenticationManager authenticationManager;
    AbstractUserRepository abstractUserRepository;
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    JwtUtils jwtUtils;
    SendEmailService sendEmailService;
    OrganiserMapper organiserMapper;
    PersonRepository personRepository;
    LegalPersonRepository legalPersonRepository;
    TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public JwtResponse authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Optional<AbstractUser> optionalUser = abstractUserRepository.findByEmail(loginRequest.getEmail());

        if (optionalUser.isEmpty()) {
            throw new BadRequestException("Invalid credentials");
        }
        if (!optionalUser.get().isEnabled()) {
            throw new BadRequestException("Please activate your account before using the app!");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(),
                optionalUser.get().getFullName(), roles);
    }

    @PostMapping("/users/signup")
    public void registerUser(@Valid @RequestBody UserSignupRequest userSignupRequest)
            throws MessagingException, UnsupportedEncodingException {
        if (abstractUserRepository.existsByEmail(userSignupRequest.getEmail())) {
            throw new BadRequestException("This email is already in use!");
        }

        User user = new User(userSignupRequest.getFirstName(), userSignupRequest.getLastName(), userSignupRequest.getEmail(),
                passwordEncoder.encode(userSignupRequest.getPassword()));

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new NotFoundException("USER role not found!"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);

        sendEmailService.sendVerificationEmail(user);
    }

    @PostMapping("/organisers/signup")
    public void registerPerson(@Valid @RequestBody PersonSignupRequest request)
            throws MessagingException, UnsupportedEncodingException {
        if (abstractUserRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("This email is already in use!");
        }

        var role = roleRepository.findByName("ORGANISER")
                .orElseThrow(() -> new NotFoundException("ORGANISER role not found!"));
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        request.setPassword(passwordEncoder.encode(request.getPassword()));
        var person = organiserMapper.personSignupRequestToPerson(request);
        person.setStatus(OrganiserStatus.PENDING);
        person.setRoles(roles);
        person.setVerificationCode(UUID.randomUUID());
        personRepository.save(person);

        sendEmailService.sendVerificationEmail(person);
    }

    @PostMapping("/organisers/legal/signup")
    public void registerLegalPerson(@Valid @RequestBody LegalPersonSignupRequest request)
            throws MessagingException, UnsupportedEncodingException {
        if (abstractUserRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("This email is already in use!");
        }

        var role = roleRepository.findByName("ORGANISER")
                .orElseThrow(() -> new NotFoundException("ORGANISER role is not found."));
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        request.setPassword(passwordEncoder.encode(request.getPassword()));
        var legalPerson = organiserMapper.legalPersonSignupRequestToLegalPerson(request);
        legalPerson.setStatus(OrganiserStatus.PENDING);
        legalPerson.setRoles(roles);
        legalPerson.setVerificationCode(UUID.randomUUID());
        legalPersonRepository.save(legalPerson);

        sendEmailService.sendVerificationEmail(legalPerson);
    }

    @PostMapping("/verify")
    public void verifyUser(@RequestParam UUID code) {
        Optional<AbstractUser> optionalUser = abstractUserRepository.findByVerificationCode(code);
        if (optionalUser.isEmpty()) {
            throw new BadRequestException("The verification code is not valid!");
        }

        AbstractUser abstractUser = optionalUser.get();
        if (abstractUser.isEnabled()) {
            throw new BadRequestException("Your account has already been activated!");
        }

        abstractUser.setEnabled(true);
        abstractUserRepository.save(abstractUser);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            Date expirationDate = jwtUtils.getExpirationDateFromJwtToken(jwt);
            LocalDateTime expirationLocalDateTime = expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            tokenBlacklistService.blacklistToken(jwt, expirationLocalDateTime);
            SecurityContextHolder.clearContext();
            return;
        }
        throw new BadRequestException("Invalid token!");
    }
}
