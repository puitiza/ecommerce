package com.ecommerce.authservice.component.initialization;


import com.ecommerce.authservice.model.entity.ERole;
import com.ecommerce.authservice.model.entity.RoleEntity;
import com.ecommerce.authservice.model.entity.UserEntity;
import com.ecommerce.authservice.model.request.SignUpRequest;
import com.ecommerce.authservice.service.RoleService;
import com.ecommerce.authservice.configuration.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.ecommerce.authservice.component.initialization.InitialDataLoader.Constant.newLine;


@Slf4j
@Component
@RequiredArgsConstructor
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private final RoleService roleService;
    private final UserDetailsServiceImpl userDetailsService;
    static class Constant {
        public static final String NUMBER = "12345678";
        public static final String NEW_TAB = "\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t";

        public static String newLine() {
            return NEW_TAB;
        }
    }

    @Transactional
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        // == create initial Role
        var role1 = createRoleIfNotFound(ERole.ROLE_ADMIN);
        var role2 = createRoleIfNotFound(ERole.ROLE_USER);
        var role3 = createRoleIfNotFound(ERole.ROLE_MODERATOR);
        var role4 = createRoleIfNotFound(ERole.ROLE_DIRECTOR);
        var role5 = createRoleIfNotFound(ERole.ROLE_MANAGER);
        var role6 = createRoleIfNotFound(ERole.ROLE_SUPPORT);

        log.debug("These are roles created by default: "
                + newLine() + role1.toString() + newLine() + role2.toString()
                + newLine() + role3.toString() + newLine() + role4.toString()
                + newLine() + role5.toString() + newLine() + role6.toString()
        );

        // == create initial user
        var user1 = createUserIfNotFound(new SignUpRequest("admin", "admin@ecommerce.com",
                Constant.NUMBER, ERole.ROLE_ADMIN.name())
        );
        var user2 = createUserIfNotFound(new SignUpRequest("user", "user@ecommerce.com",
                Constant.NUMBER, ERole.ROLE_USER.name())
        );
        var user3 = createUserIfNotFound(new SignUpRequest("moderator", "moderator@ecommerce.com",
                Constant.NUMBER, ERole.ROLE_MODERATOR.name())
        );

        log.debug("These are roles created by default: "
                + newLine() + user1.toString() + newLine() + user2.toString() + newLine() + user3.toString()
        );

    }

    @Transactional
    public RoleEntity createRoleIfNotFound(ERole name) {
        var role = roleService.getRole(name);
        return role.orElseGet(() -> roleService.addRole(name));
    }

    @Transactional
    public UserEntity createUserIfNotFound(SignUpRequest request) {
        var user = userDetailsService.getUserByUsername(request.getUsername());
        return user.orElseGet(() -> userDetailsService.addUser(request));
    }
}
