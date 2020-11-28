package com.tsb.restproxy.otp.verification.service;


import com.google.gson.Gson;
import com.tsb.ob.restproxy.avro.event.RestProxyEventRequest;
import com.tsb.restproxy.otp.verification.config.KafkaConfigProperties;
import com.tsb.restproxy.otp.verification.dto.OtpVerification;
import com.tsb.restproxy.otp.verification.publishers.OtpVerificationPublisher;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.UUID;

import static com.tsb.ob.restproxy.avro.event.EventType.VERIFY_OTP;
import static com.tsb.ob.restproxy.avro.event.RequestType.AUTH;
import static com.tsb.onboarding.avro.util.ComponentName.REST_PROXY_OTP_VERIFICATION;

@Slf4j
@Service
@AllArgsConstructor
public class OtpVerificationService {

    private final KafkaConfigProperties kafkaConfigProperties;
    private final OtpVerificationPublisher otpVerificationPublisher;

    public String sendAuthEventRequest(OtpVerification otpVerification) {
        String correlationId = UUID.randomUUID().toString();
        RestProxyEventRequest restProxyEventRequest = getGenericEventRequest(otpVerification,correlationId);
        log.debug("publishing restProxyEventRequest: {}", restProxyEventRequest);
        otpVerificationPublisher.sendAuthEventRequestToKafka(restProxyEventRequest);
        return correlationId;
    }

    private RestProxyEventRequest getGenericEventRequest(OtpVerification otpVerification, String correlationId) {
        RestProxyEventRequest request = new RestProxyEventRequest();
        request.setUserId(otpVerification.getUserId());
        request.setCorrelationId(correlationId);
        request.setHmac(otpVerification.getHmac());
        request.setCreatedBy(REST_PROXY_OTP_VERIFICATION);
        request.setCreatedAt(Clock.systemUTC().millis());
        request.setRequestParams(new Gson().toJson(otpVerification));
        request.setRequestType(AUTH);
        request.setEventType(VERIFY_OTP);
        request.setReplyTo(kafkaConfigProperties.getSource());
        request.setThroughTo(kafkaConfigProperties.getSinkThrough());
        return request;
    }
}
