package com.unibuc.fmi.eventful.repository;

import com.unibuc.fmi.eventful.model.AbstractUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AbstractUserRepository extends JpaRepository<AbstractUser, Long> {

    boolean existsByEmail(String email);

    Optional<AbstractUser> findByEmail(String email);

    Optional<AbstractUser> findByVerificationCode(UUID verificationCode);
}
