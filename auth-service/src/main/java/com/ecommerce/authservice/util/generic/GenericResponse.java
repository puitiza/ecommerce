package com.ecommerce.authservice.util.generic;

import com.ecommerce.authservice.model.exception.GlobalErrorResponse;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericResponse<T, U> {
    /**
     * Contains the specific response data object.
     */
    private T data;
    /**
     * Indicate the operation was a success true or have errors false
     */
    private boolean success;
    /**
     * Exception detail if exist.
     */
    private U exception;

    public static Object createErrorMessageDTO(GlobalErrorResponse errorMessage) {
        return GenericResponse.builder()
                .success(false)
                .exception(errorMessage).build();
    }
}
