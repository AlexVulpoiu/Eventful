package com.unibuc.fmi.eventful.controllers;

import com.unibuc.fmi.eventful.dto.request.signin.LoginRequest;
import com.unibuc.fmi.eventful.dto.request.signup.LegalPersonSignupRequest;
import com.unibuc.fmi.eventful.dto.request.signup.PersonSignupRequest;
import com.unibuc.fmi.eventful.dto.request.signup.UserSignupRequest;
import com.unibuc.fmi.eventful.dto.response.JwtResponse;
import com.unibuc.fmi.eventful.dto.response.MessageResponse;
import com.unibuc.fmi.eventful.exceptions.BadRequestException;
import com.unibuc.fmi.eventful.mappers.OrganiserMapper;
import com.unibuc.fmi.eventful.model.AbstractUser;
import com.unibuc.fmi.eventful.model.Role;
import com.unibuc.fmi.eventful.model.User;
import com.unibuc.fmi.eventful.repository.*;
import com.unibuc.fmi.eventful.security.jwt.JwtUtils;
import com.unibuc.fmi.eventful.security.services.UserDetailsImpl;
import com.unibuc.fmi.eventful.services.SendEmailService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
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
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserSignupRequest userSignupRequest)
            throws MessagingException, UnsupportedEncodingException {
        if (abstractUserRepository.existsByEmail(userSignupRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User(userSignupRequest.getFirstName(), userSignupRequest.getLastName(), userSignupRequest.getEmail(),
                passwordEncoder.encode(userSignupRequest.getPassword()));

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);

        sendEmailService.sendVerificationEmail(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/organisers/signup")
    public ResponseEntity<?> registerPerson(@Valid @RequestBody PersonSignupRequest request)
            throws MessagingException, UnsupportedEncodingException {
        if (abstractUserRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        var role = roleRepository.findByName("ORGANISER")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        request.setPassword(passwordEncoder.encode(request.getPassword()));
        var person = organiserMapper.personSignupRequestToPerson(request);
        person.setRoles(roles);
        person.setVerificationCode(UUID.randomUUID());
        personRepository.save(person);

        sendEmailService.sendVerificationEmail(person);

        return ResponseEntity.ok(new MessageResponse("Person registered successfully!"));
    }

    @PostMapping("/organisers/legal/signup")
    public ResponseEntity<?> registerLegalPerson(@Valid @RequestBody LegalPersonSignupRequest request)
            throws MessagingException, UnsupportedEncodingException {
        if (abstractUserRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        var role = roleRepository.findByName("ORGANISER")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        request.setPassword(passwordEncoder.encode(request.getPassword()));
        var legalPerson = organiserMapper.legalPersonSignupRequestToLegalPerson(request);
        legalPerson.setRoles(roles);
        legalPerson.setVerificationCode(UUID.randomUUID());
        legalPersonRepository.save(legalPerson);

        sendEmailService.sendVerificationEmail(legalPerson);

        return ResponseEntity.ok(new MessageResponse("Legal person registered successfully!"));
    }

    @GetMapping("/verify")
    public ResponseEntity<MessageResponse> verifyUser(@RequestParam UUID code) {
        Optional<AbstractUser> optionalUser = abstractUserRepository.findByVerificationCode(code);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("The verification code is not valid!"));
        }

        AbstractUser abstractUser = optionalUser.get();
        if (abstractUser.isEnabled()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Your account has already been activated!"));
        }

        abstractUser.setEnabled(true);
        abstractUserRepository.save(abstractUser);
        return ResponseEntity.ok(new MessageResponse("Your account has been successfully activated!"));
    }
}
