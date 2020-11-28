package com.tsb.restproxy.otp.verification.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpVerification {

    @NotEmpty(message = "Invalid data")
    @Pattern(regexp = "^[0-9]{1,6}$")
    @JsonProperty
    private String otp;

    @Nullable
    @JsonProperty
    private String userId;

    @Nullable
    @JsonProperty
    private String hmac;

}
