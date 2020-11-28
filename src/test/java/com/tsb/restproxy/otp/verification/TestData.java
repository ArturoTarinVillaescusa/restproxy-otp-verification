package com.tsb.restproxy.otp.verification;

import com.tsb.restproxy.otp.verification.dto.OtpVerification;
import com.tsb.restproxy.otp.verification.dto.OtpVerificationResponse;

public class TestData {

    public static OtpVerification createDummyOtp(String otp, String userId, String hmac) {
        return new OtpVerification(otp, userId, hmac);
    }

    public static OtpVerificationResponse populateOtpVerificationResponse(String code, String reason) {
        OtpVerificationResponse otpVerificationResponse = new OtpVerificationResponse();
        otpVerificationResponse.setCode(code);
        otpVerificationResponse.setReason(reason);
        return otpVerificationResponse;
    }
}
