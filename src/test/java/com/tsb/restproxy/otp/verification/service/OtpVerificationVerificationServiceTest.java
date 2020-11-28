package com.tsb.restproxy.otp.verification.service;

import com.google.gson.Gson;
import com.tsb.ob.restproxy.avro.event.RestProxyEventRequest;
import com.tsb.restproxy.otp.verification.config.KafkaConfigProperties;
import com.tsb.restproxy.otp.verification.dto.OtpVerification;
import com.tsb.restproxy.otp.verification.publishers.OtpVerificationPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.tsb.restproxy.otp.verification.TestData.createDummyOtp;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OtpVerificationVerificationServiceTest {

    @InjectMocks
    private OtpVerificationService underTest;

    @Mock
    private KafkaConfigProperties kafkaConfigProperties;

    @Mock
    private OtpVerificationPublisher otpVerificationPublisher;

    @Test
    public void should_publish_auth_event_request_to_kafka() {
        OtpVerification otpVerification = createDummyOtp("23452", "userId013423", "hmacxsljer");
        String zookeeperServer = "localhost:2182";
        lenient().when(kafkaConfigProperties.getZooKeeperServer()).thenReturn(zookeeperServer);
        lenient().when(kafkaConfigProperties.getSource()).thenReturn("topic");

        underTest.sendAuthEventRequest(otpVerification);

        verify(otpVerificationPublisher)
                .sendAuthEventRequestToKafka(argThat(restProxyEventRequestMatcher(otpVerification)));
    }

    private ArgumentMatcher<RestProxyEventRequest> restProxyEventRequestMatcher(OtpVerification otpVerification) {
        return arg -> arg.getRequestParams().equals(new Gson().toJson(otpVerification));
    }
}