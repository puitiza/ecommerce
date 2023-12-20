package com.ecommerce.authservice.repository;

import com.ecommerce.authservice.model.entity.RefreshTokenEntity;
import com.ecommerce.authservice.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findById(Long id);

    Optional<RefreshTokenEntity> findByToken(String token);

    Optional<RefreshTokenEntity> findByUsers(UserEntity user);

}
