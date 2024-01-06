package com.ecommerce.userservice.model.templateAPI;

/**
 * Swagger api documentation requires these classes to display json models of the responses.
 * Each class's javadoc is what Swagger will translate them into on the ui.
 */
public class AuthResponseTemplate {

    public static final String UNPROCESSABLE = """
            {
                "status": 422,
                "timestamp": "04-01-2024 04:55:12 PET",
                "errorCode": "P01",
                "message": "Validation error. Check 'errors' field for details.",
                "detailMessage": "",
                "stackTrace": [
                    "org.springframework.web.bind.MethodArgumentNotValidException Validation failed for argument [0] in public org.springframework.http.ResponseEntity<com.ecommerce.userservice.model.response.LoginResponse> com.ecommerce.userservice.controller.UserController.login(com.ecommerce.userservice.model.request.LoginRequest): [Field error in object 'loginRequest' on field 'username': rejected value []; codes [NotBlank.loginRequest.username,NotBlank.username,NotBlank.java.lang.String,NotBlank]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [loginRequest.username,username]; arguments []; default message [username]]; default message ['username' field not should be null or empty]] ",
                    "org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor.resolveArgument(RequestResponseBodyMethodProcessor.java:143)",
                    "org.springframework.web.method.support.HandlerMethodArgumentResolverComposite.resolveArgument(HandlerMethodArgumentResolverComposite.java:122)",
                    "org.springframework.web.method.support.InvocableHandlerMethod.getMethodArgumentValues(InvocableHandlerMethod.java:218)",
                    "org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:171)"
                ],
                "errors": [
                    {
                        "field": "username",
                        "message": "'username' field not should be null or empty"
                    }
                ]
            }
            """;
    public static final String UNAUTHORIZED = """
            {
                "status": 401,
                "timestamp": "04-01-2024 07:23:34 PET",
                "errorCode": "P02",
                "message": "An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature",
                "detailMessage": "",
                "stackTrace": [
                    "org.springframework.security.oauth2.server.resource.InvalidBearerTokenException An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature",
                    "org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider.getJwt(JwtAuthenticationProvider.java:103)",
                    "org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider.authenticate(JwtAuthenticationProvider.java:88)",
                    "org.springframework.security.authentication.ProviderManager.authenticate(ProviderManager.java:182)",
                    "org.springframework.security.authentication.ObservationAuthenticationManager.lambda$authenticate$1(ObservationAuthenticationManager.java:54)"
                ]
            }
            """;
}
