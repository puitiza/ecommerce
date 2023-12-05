package com.ecommerce.authservice.service;

import com.ecommerce.authservice.model.entity.ERole;
import com.ecommerce.authservice.model.entity.RoleEntity;
import com.ecommerce.authservice.repository.RoleRepository;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
public record RoleService(RoleRepository repository) {

    public Optional<RoleEntity> getRole(ERole name) {
        return repository.findByName(name);
    }


    public RoleEntity addRole(ERole name) {
        var role = new RoleEntity();

        role.setName(name);
        return repository.save(role);

    }
}
