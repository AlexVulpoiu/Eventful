package com.unibuc.fmi.eventful.controllers;

import com.unibuc.fmi.eventful.dto.auth.JwtResponse;
import com.unibuc.fmi.eventful.dto.auth.LoginRequest;
import com.unibuc.fmi.eventful.dto.auth.MessageResponse;
import com.unibuc.fmi.eventful.dto.auth.SignupRequest;
import com.unibuc.fmi.eventful.model.Role;
import com.unibuc.fmi.eventful.model.User;
import com.unibuc.fmi.eventful.repository.RoleRepository;
import com.unibuc.fmi.eventful.repository.UserRepository;
import com.unibuc.fmi.eventful.security.jwt.JwtUtils;
import com.unibuc.fmi.eventful.security.services.UserDetailsImpl;
import com.unibuc.fmi.eventful.services.SendEmailService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtils jwtUtils;

    private final SendEmailService sendEmailService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Optional<User> optionalUser = userRepository.findByEmail(loginRequest.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid credentials"));
        }
        if (!optionalUser.get().isEnabled()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Please activate your account before using the app."));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest)
            throws MessagingException, UnsupportedEncodingException {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User(signupRequest.getFirstName(), signupRequest.getLastName(), signupRequest.getEmail(),
                passwordEncoder.encode(signupRequest.getPassword()));

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);

        sendEmailService.sendVerificationEmail(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/verify")
    public ResponseEntity<MessageResponse> verifyUser(@RequestParam UUID code) {
        Optional<User> optionalUser = userRepository.findByVerificationCode(code);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("The verification code is not valid!"));
        }

        User user = optionalUser.get();
        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Your account has already been activated!"));
        }

        user.setEnabled(true);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("Your account has been successfully activated!"));
    }
}
