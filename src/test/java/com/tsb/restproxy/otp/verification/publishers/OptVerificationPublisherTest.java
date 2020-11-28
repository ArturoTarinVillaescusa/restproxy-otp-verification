package com.tsb.restproxy.otp.verification.publishers;

import com.tsb.ob.restproxy.avro.event.RestProxyEventRequest;
import com.tsb.restproxy.otp.verification.config.KafkaConfigProperties;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;

import static com.tsb.ob.restproxy.avro.event.EventType.VERIFY_OTP;
import static com.tsb.ob.restproxy.avro.event.RequestType.AUTH;
import static com.tsb.onboarding.avro.util.ComponentName.REST_PROXY_OTP_VERIFICATION;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OptVerificationPublisherTest {

    @Mock
    private KafkaConfigProperties kafkaConfigProperties;

    @Mock
    private Producer<String, RestProxyEventRequest> producer;

    @InjectMocks
    private OtpVerificationPublisher underTest;

    @Test
    public void should_send_auth_event_request() {

        String userId = "12345";
        String topic = "topic";
        RestProxyEventRequest restProxyEventRequest = createRestProxyEventRequest(userId);

        given(kafkaConfigProperties.getSink()).willReturn(topic);

        underTest.sendAuthEventRequestToKafka(restProxyEventRequest);

        verify(producer).send(argThat(producerRecordMatcher(userId,topic)));
    }

    private RestProxyEventRequest createRestProxyEventRequest(String userId) {
        return RestProxyEventRequest.newBuilder()
                .setCorrelationId("correlationId")
                .setCreatedBy(REST_PROXY_OTP_VERIFICATION)
                .setCreatedAt(Clock.systemUTC().millis())
                .setRequestParams("data")
                .setRequestType(AUTH)
                .setUserId(userId)
                .setHmac("HMAC-00001")
                .setEventType(VERIFY_OTP)
                .setReplyTo("topic")
                .setThroughTo("througTopic")
                .build();
    }

    private ArgumentMatcher<ProducerRecord<String, RestProxyEventRequest>>
    producerRecordMatcher(String userId, String topic) {
        return argument -> argument.key().equals(userId) &&
                argument.value().getUserId().equals(userId) &&
                argument.value().getReplyTo().equals(topic);
    }
}