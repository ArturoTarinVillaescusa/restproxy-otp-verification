package com.tsb.restproxy.otp.verification.utils;

import com.tsb.restproxy.otp.verification.dto.OtpVerificationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.async.DeferredResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
public class OtpVerificationVerificationUtilsTest {

    @InjectMocks
    private OtpVerificationUtils underTest;

    @Test
    void should_return_true_when_pending_request_is_present() {
        String correlationId = "12345";
        OtpVerificationUtils.PENDING_REQUEST_CACHE.put(correlationId,new DeferredResult(1000L));
        boolean result = underTest.isPendingRequestPresent(correlationId);
        assertTrue(result);

        result = underTest.isPendingRequestPresent("wrong_key");
        assertFalse(result);
    }

    @Test
    void setResponse() {
        String correlationId = "12345";
        OtpVerificationUtils.PENDING_REQUEST_CACHE.put(correlationId,new DeferredResult(1000L));

        underTest.setResponse(correlationId, otpVerificationResponse(), HttpStatus.OK);

        assertFalse(underTest.isPendingRequestPresent(correlationId));
    }

    private OtpVerificationResponse otpVerificationResponse() {
        OtpVerificationResponse otpVerificationResponse = new OtpVerificationResponse();
        return otpVerificationResponse;
    }
}