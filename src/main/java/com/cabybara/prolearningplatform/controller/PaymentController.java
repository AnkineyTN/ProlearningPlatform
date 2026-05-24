package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.payment.CreatePaymentLinkRequestDto;
import com.cabybara.prolearningplatform.dto.response.payment.CreatePaymentResponse;
import com.cabybara.prolearningplatform.dto.response.payment.PaymentStatusResponse;
import com.cabybara.prolearningplatform.service.payment.PayOSService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Tag(name = "Payment")
public class PaymentController {

    private final PayOSService payOSService;

    @Operation(
            summary = "Create a PayOS payment link for PRO upgrade",
            description = "Creates a payment link via PayOS. The `platform` field must be either `\"web\"` or `\"mobile\"` — " +
                    "this determines the redirect URLs used after payment completion or cancellation."
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CreatePaymentResponse>> createPayment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreatePaymentLinkRequestDto createPaymentLinkRequestDto) {
        CreatePaymentResponse response = payOSService.createPaymentLink(userIdFromJwt(jwt), createPaymentLinkRequestDto.platform());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Payment link created", response, null));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cancel/{orderCode}")
    public ResponseEntity<ApiResponse<Void>> cancelPayment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable long orderCode) {
        payOSService.cancelPaymentLink(userIdFromJwt(jwt), orderCode);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Order cancelled", null, null));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/status/{orderCode}")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable long orderCode) {
        PaymentStatusResponse response = payOSService.getPaymentStatus(userIdFromJwt(jwt), orderCode);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Payment status retrieved", response, null));
    }

    private static Long userIdFromJwt(Jwt jwt) {
        return Long.parseLong(jwt.getClaims().get("id").toString());
    }
}
