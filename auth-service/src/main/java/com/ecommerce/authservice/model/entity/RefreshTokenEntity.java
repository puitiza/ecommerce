package com.ecommerce.authservice.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter
@Entity(name = "refresh_token")
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne()
    @MapsId
    @JoinColumn(name = "users_id")
    private UserEntity users;

    private String token;

    private Instant expiryDate;


}