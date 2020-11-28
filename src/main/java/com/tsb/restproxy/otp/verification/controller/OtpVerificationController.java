package com.tsb.restproxy.otp.verification.controller;

import com.tsb.restproxy.otp.verification.dto.OtpVerification;
import com.tsb.restproxy.otp.verification.dto.OtpVerificationResponse;
import com.tsb.restproxy.otp.verification.service.OtpVerificationService;
import com.tsb.restproxy.otp.verification.utils.OtpVerificationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OtpVerificationController {

    @Value("${request.expiry.time}")
    private Long timeout;

    private final OtpVerificationService otpVerificationService;

    @PostMapping(path = "/users/{userId}/phone/verify", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public DeferredResult<OtpVerificationResponse> validateOtp(@Validated @RequestBody OtpVerification otpVerification, @RequestHeader("X-Authorization-HMAC") String xAuthorizationHMAC, @PathVariable String userId)  {
        log.debug("start validateOtp for userId: {}", userId);
        return sendAuthRequest(otpVerification.getOtp(), userId, xAuthorizationHMAC);
    }

    private DeferredResult<OtpVerificationResponse> getDeferredResult(){
        final DeferredResult<OtpVerificationResponse> deferredResult = new DeferredResult<>(timeout);
        deferredResult.onTimeout(() -> deferredResult.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build()));
        return deferredResult;
    }

    private DeferredResult<OtpVerificationResponse> sendAuthRequest(String otp, String userId, String hmac) {
        OtpVerification otpVerificationInstance = new OtpVerification(otp, userId, hmac);
        String correlationId = otpVerificationService.sendAuthEventRequest(otpVerificationInstance);
        final DeferredResult<OtpVerificationResponse> deferredResult = getDeferredResult();
        OtpVerificationUtils.PENDING_REQUEST_CACHE.put(correlationId, deferredResult);
        return deferredResult;
    }
}
