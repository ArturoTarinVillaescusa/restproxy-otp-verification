package com.tsb.restproxy.otp.verification.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OtpVerificationRequest {

    private String otp;
    private String xAuthorizationHMAC;
    private String userId;

}
