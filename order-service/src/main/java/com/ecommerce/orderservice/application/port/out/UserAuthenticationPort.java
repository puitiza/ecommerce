package com.ecommerce.orderservice.application.port.out;

import com.ecommerce.orderservice.application.dto.UserAuthenticationDetails;

public interface UserAuthenticationPort {

    UserAuthenticationDetails getUserDetails();

}
