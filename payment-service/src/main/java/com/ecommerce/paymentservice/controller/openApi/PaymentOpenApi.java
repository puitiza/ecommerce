package com.ecommerce.paymentservice.controller.openApi;

import com.ecommerce.paymentservice.model.exception.ResponseApiTemplate;
import com.ecommerce.paymentservice.model.request.PaymentAuthorizationRequest;
import com.ecommerce.paymentservice.model.request.PaymentRequest;
import com.ecommerce.paymentservice.model.request.PaymentTransactionDetails;
import com.ecommerce.paymentservice.model.request.RefundRequest;
import com.ecommerce.paymentservice.model.response.PaymentAuthorizationResponse;
import com.ecommerce.paymentservice.model.response.PaymentResponse;
import com.ecommerce.paymentservice.model.response.RefundResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.UUID;

@SuppressWarnings("ALL")
public interface PaymentOpenApi {

    @Operation(summary = "Process Payment", description = "Processes a payment for an order")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment processed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.UNAUTHORIZED))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.FORBIDDEN))),
            @ApiResponse(responseCode = "422", description = "Invalid payment authorization request",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.UNPROCESSABLE))),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.RATE_LIMIT)))
    })
    PaymentResponse processPayment(PaymentRequest paymentRequest);

    @Operation(summary = "Get Payment Details", description = "Gets details of a specific payment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment details retrieved successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PaymentTransactionDetails.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.UNAUTHORIZED))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "Payment not found",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.NOT_FOUND))),
    })
    PaymentTransactionDetails getPaymentDetails(UUID paymentId);

    @Operation(summary = "Authorize Payment", description = "Authorizes a payment for an order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment authorization successful",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PaymentAuthorizationResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.UNAUTHORIZED))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.FORBIDDEN))),
            @ApiResponse(responseCode = "422", description = "Invalid payment authorization request",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.UNPROCESSABLE)))
    })
    PaymentAuthorizationResponse authorizePayment(PaymentAuthorizationRequest authorizationRequest);

    @Operation(summary = "Initiate Refund", description = "Initiates a refund for a payment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refund initiated successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = RefundResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.UNAUTHORIZED))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "Payment not found",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.NOT_FOUND))),
            @ApiResponse(responseCode = "422", description = "Invalid refund request",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.UNPROCESSABLE)))
    })
    RefundResponse initiateRefund(UUID paymentId, RefundRequest refundRequest);
}
