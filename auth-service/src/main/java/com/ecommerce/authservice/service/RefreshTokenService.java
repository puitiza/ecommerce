package com.ecommerce.authservice.service;

import com.ecommerce.authservice.component.exception.handler.NoSuchElementFoundException;
import com.ecommerce.authservice.component.exception.handler.TokenRefreshException;
import com.ecommerce.authservice.configuration.security.jwt.JwtUtils;
import com.ecommerce.authservice.model.entity.RefreshTokenEntity;
import com.ecommerce.authservice.model.entity.UserEntity;
import com.ecommerce.authservice.model.request.TokenRefreshRequest;
import com.ecommerce.authservice.model.response.TokenRefreshResponse;
import com.ecommerce.authservice.repository.RefreshTokenRepository;
import com.ecommerce.authservice.repository.UserRepository;
import com.ecommerce.authservice.util.message_source.MessageSourceHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.ecommerce.authservice.component.exception.errors.GlobalExceptionErrors.*;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refreshExpirationDateInMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    private final MessageSourceHandler messageSource;

    public Optional<RefreshTokenEntity> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public UserEntity findByUserId(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NoSuchElementFoundException(
                messageSource.getLocalMessage(NO_ITEM_FOUND.getKey(), String.valueOf(userId)),
                messageSource.getLocalMessage(NO_ITEM_FOUND.getCode()))
        );
    }

    public RefreshTokenEntity createRefreshToken(Long userId) {
        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setUsers(findByUserId(userId));
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshTokenEntity verifyExpiration(RefreshTokenEntity token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(
                    messageSource.getLocalMessage(REFRESH_TOKEN_EXPIRED_ERROR.getKey()),
                    messageSource.getLocalMessage(REFRESH_TOKEN_EXPIRED_ERROR.getCode())
            );
        }
        return token;
    }

    public TokenRefreshResponse refreshToken(TokenRefreshRequest request, JwtUtils jwtUtils) {
        String requestRefreshToken = request.getRefreshToken();
        return findByToken(requestRefreshToken)
                .map(this::verifyExpiration)
                .map(RefreshTokenEntity::getUsers)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    return new TokenRefreshResponse(token, requestRefreshToken);
                })
                .orElseThrow(
                        () -> new TokenRefreshException(
                                messageSource.getLocalMessage(REFRESH_TOKEN_ERROR.getKey()),
                                messageSource.getLocalMessage(REFRESH_TOKEN_ERROR.getCode())
                        )
                );
    }

}
