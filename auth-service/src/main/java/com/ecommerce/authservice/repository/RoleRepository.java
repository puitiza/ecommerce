package com.ecommerce.authservice.repository;

import com.ecommerce.authservice.model.entity.ERole;
import com.ecommerce.authservice.model.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(ERole name);

}
