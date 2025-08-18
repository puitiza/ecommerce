package com.ecommerce.paymentservice.controller.openApi;

import com.ecommerce.paymentservice.model.request.PaymentAuthorizationRequest;
import com.ecommerce.paymentservice.model.request.PaymentRequest;
import com.ecommerce.paymentservice.model.request.PaymentTransactionDetails;
import com.ecommerce.paymentservice.model.request.RefundRequest;
import com.ecommerce.paymentservice.model.response.PaymentAuthorizationResponse;
import com.ecommerce.paymentservice.model.response.PaymentResponse;
import com.ecommerce.paymentservice.model.response.RefundResponse;
import com.ecommerce.shared.interfaces.openapi.response.ApiResourceNotFound;
import com.ecommerce.shared.interfaces.openapi.response.ApiValidationErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.UUID;

@SuppressWarnings("unused")
public interface PaymentOpenApi {

    @ApiValidationErrors
    @Operation(summary = "Process Payment", description = "Processes a payment for an order")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment processed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))}),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    PaymentResponse processPayment(PaymentRequest paymentRequest);

    @ApiResourceNotFound
    @Operation(summary = "Get Payment Details", description = "Gets details of a specific payment")
    @ApiResponse(responseCode = "200", description = "Payment details retrieved successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PaymentTransactionDetails.class))})
    PaymentTransactionDetails getPaymentDetails(UUID paymentId);

    @ApiValidationErrors
    @Operation(summary = "Authorize Payment", description = "Authorizes a payment for an order")
    @ApiResponse(responseCode = "200", description = "Payment authorization successful",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PaymentAuthorizationResponse.class))})
    PaymentAuthorizationResponse authorizePayment(PaymentAuthorizationRequest authorizationRequest);

    @ApiValidationErrors
    @Operation(summary = "Initiate Refund", description = "Initiates a refund for a payment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refund initiated successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = RefundResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    RefundResponse initiateRefund(UUID paymentId, RefundRequest refundRequest);
}
