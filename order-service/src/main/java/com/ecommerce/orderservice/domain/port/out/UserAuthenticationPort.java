package com.ecommerce.orderservice.domain.port.out;

import com.ecommerce.orderservice.infrastructure.adapter.security.dto.UserAuthenticationDetails;

public interface UserAuthenticationPort {

    UserAuthenticationDetails getUserDetails();

}
