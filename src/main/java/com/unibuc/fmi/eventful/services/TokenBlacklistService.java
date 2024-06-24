package com.unibuc.fmi.eventful.services;

import com.unibuc.fmi.eventful.model.BlacklistedToken;
import com.unibuc.fmi.eventful.repository.BlacklistedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TokenBlacklistService {

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    public void blacklistToken(String token, LocalDateTime expirationDate) {
        if (!isTokenBlacklisted(token)) {
            BlacklistedToken blacklistedToken = new BlacklistedToken();
            blacklistedToken.setToken(token);
            blacklistedToken.setExpirationDate(expirationDate);
            blacklistedTokenRepository.save(blacklistedToken);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        Optional<BlacklistedToken> blacklistedToken = blacklistedTokenRepository.findByToken(token);
        return blacklistedToken.isPresent() && blacklistedToken.get().getExpirationDate().isAfter(LocalDateTime.now());
    }
}
