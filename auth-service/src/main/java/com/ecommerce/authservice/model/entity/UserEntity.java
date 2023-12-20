package com.ecommerce.authservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @ManyToMany
    @JoinTable(name = "users_role",
            joinColumns = @JoinColumn(name = "users_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Set<RoleEntity> roles = new HashSet<>();

    @JsonIgnore
    @OneToOne(mappedBy = "users")
    private RefreshTokenEntity refreshToken;

    public UserEntity(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public UserEntity() {

    }

    @Override
    public String toString() {
        return "UserEntity(id=" + this.id + ", username=" + this.username + ", email=" + this.email +
                ", password=" + 12345678 + ",roles=" + this.roles + ")";
    }

    public void addRole(RoleEntity role) {
        this.getRoles().add(role);
        role.getUsers().add(this);
    }

    public void removeRole(RoleEntity role) {
        this.getRoles().remove(role);
        role.getUsers().remove(this);
    }

}
