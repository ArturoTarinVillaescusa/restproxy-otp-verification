package com.tsb.restproxy.otp.verification.controller;


import com.google.gson.Gson;
import com.tsb.restproxy.otp.verification.dto.OtpVerification;
import com.tsb.restproxy.otp.verification.service.OtpVerificationService;
import com.tsb.restproxy.otp.verification.utils.OtpVerificationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockAsyncContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.AsyncListener;

import static com.tsb.restproxy.otp.verification.TestData.createDummyOtp;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class OtpVerificationVerificationControllerTest {

    @InjectMocks
    private OtpVerificationController underTest;

    @Mock
    private OtpVerificationService otpVerificationService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        OtpVerificationUtils.PENDING_REQUEST_CACHE.remove("12345");
        this.mockMvc = MockMvcBuilders.standaloneSetup(underTest).build();
    }

    @Test
    public void should_create_auth_event_type_otp_validation() throws Exception {
        OtpVerification otpVerification = createDummyOtp("123456","user01","hmac01");

        String correlationId = "12345";

        when(otpVerificationService
                .sendAuthEventRequest(otpVerification))
                .thenReturn(correlationId);

        MvcResult result = this.mockMvc.perform(post("/users/user01/phone/verify")
                .header("X-Authorization-HMAC", "hmac01")
                .content(getContent(otpVerification))
                .contentType(APPLICATION_JSON))
                .andDo(print()).andReturn();

        this.setResultInTimedRequestCache(correlationId);
        MockAsyncContext ctx = (MockAsyncContext) result.getRequest().getAsyncContext();

        for (AsyncListener listener : ctx.getListeners()) {
            listener.onTimeout(null);
        }

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    public void should_returnBadRequestWhenOtpIsNull() throws Exception {
        OtpVerification otpVerification = createDummyOtp(null, "user01", "hmac01");

        this.mockMvc.perform(post("/users/user01/phone/verify")
                .header("X-Authorization-HMAC", "hmac01")
                .content(getContent(otpVerification))
                .contentType(APPLICATION_JSON))
                .andDo(print()).andExpect(status().isBadRequest());
    }

    @Test
    public void should_returnBadRequestWhenThereIsCharacterInOtp() throws Exception {
        OtpVerification otpVerification = createDummyOtp("a123456", "user01", "hmac01");

        this.mockMvc.perform(post("/users/user01/phone/verify")
                .header("X-Authorization-HMAC", "hmac01")
                .content(getContent(otpVerification))
                .contentType(APPLICATION_JSON))
                .andDo(print()).andExpect(status().isBadRequest());
    }

    @Test
    public void should_returnBadRequestWhenOtpIsLongerThan6() throws Exception {
        OtpVerification otpVerification = createDummyOtp("1234567", "user01", "hmac01");

        this.mockMvc.perform(post("/users/user01/phone/verify")
                .header("X-Authorization-HMAC", "hmac01")
                .content(getContent(otpVerification))
                .contentType(APPLICATION_JSON))
                .andDo(print()).andExpect(status().isBadRequest());
    }

    @Test
    public void should_return_timeout() throws Exception {
        OtpVerification otpVerification = createDummyOtp("123456","user01","hmac01");

        String correlationId = "12345";

        when(otpVerificationService.sendAuthEventRequest(otpVerification)).thenReturn(correlationId);

        MvcResult result = this.mockMvc.perform(post("/users/user01/phone/verify")
                .header("X-Authorization-HMAC", "hmac01")
                .content(getContent(otpVerification))
                .contentType(APPLICATION_JSON))
                .andDo(print()).andReturn();

        MockAsyncContext ctx = (MockAsyncContext) result.getRequest().getAsyncContext();

        for (AsyncListener listener : ctx.getListeners()) {
            listener.onTimeout(null);
        }

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isRequestTimeout());
    }

    @Test
    void should_return_invalid_request_when_otp_is_less_than_six_digit() throws Exception {
        OtpVerification otpVerification = createDummyOtp("", "", "");

        mockMvc.perform(post("/users/{userId}/phone/verify", "userId")
                        .header("X-Authorization-HMAC", "hmac01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getContent(otpVerification))
                        .contentType(APPLICATION_JSON)).andExpect(status().is(400));
    }


    private String getContent(OtpVerification otpVerification) {
        return new Gson().toJson(otpVerification);
    }

    private void setResultInTimedRequestCache(String correlationId) {
        OtpVerificationUtils.PENDING_REQUEST_CACHE.get(correlationId).setResult(ResponseEntity.status(HttpStatus.OK).build());
    }

}