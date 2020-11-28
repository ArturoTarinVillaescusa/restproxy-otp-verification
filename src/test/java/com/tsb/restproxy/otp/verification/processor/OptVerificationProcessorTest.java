package com.tsb.restproxy.otp.verification.processor;

import com.google.gson.Gson;
import com.tsb.ob.restproxy.avro.event.RestProxyEventResponse;
import com.tsb.restproxy.otp.verification.TestData;
import com.tsb.restproxy.otp.verification.config.KafkaConfigProperties;
import com.tsb.restproxy.otp.verification.dto.OtpVerificationResponse;
import com.tsb.restproxy.otp.verification.utils.OtpVerificationUtils;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.tsb.onboarding.avro.util.ComponentName.REST_PROXY_OTP_VERIFICATION;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class OptVerificationProcessorTest {

    @InjectMocks
    private OtpVerificationProcessor underTest;

    @Mock
    private KafkaConfigProperties kafkaConfigProperties;

    @Mock
    private OtpVerificationUtils otpVerificationUtils;


    private static final String SCHEMA_REGISTRY_SCOPE = OptVerificationProcessorTest.class.getName();
    private static final String MOCK_SCHEMA_REGISTRY_URL = "mock://" + SCHEMA_REGISTRY_SCOPE;

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, RestProxyEventResponse> resultTopic;

    private Gson gson = new Gson();

    @BeforeEach
    public void setUpTopologyTestDriver() {

        given(kafkaConfigProperties.getSource()).willReturn("result");


        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        config.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, MOCK_SCHEMA_REGISTRY_URL);

        Topology topology = underTest.getTopology();
        testDriver = new TopologyTestDriver(topology, config);

        Serde<String> stringSerde = Serdes.String();
        Serde<RestProxyEventResponse> avroSerde = new SpecificAvroSerde<>();
        Map<String, String> serdesConfig = new HashMap<>();
        serdesConfig.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, MOCK_SCHEMA_REGISTRY_URL);
        avroSerde.configure(serdesConfig, false);

        resultTopic = testDriver.createInputTopic(
                "result",
                stringSerde.serializer(),
                avroSerde.serializer());

    }

    @AfterEach
    public void closeTestDriver() {
        testDriver.close();
    }

    @Test
    public void should_create_topology() {
        Topology topology = underTest.getTopology();
        assertNotNull(topology);
    }

    @Test
    public void should_process_otp() {
        String correlationId = "123";
        String code = "200";
        String reason = "";

        RestProxyEventResponse restProxyEventResponse = RestProxyEventResponse.newBuilder()
                .setCorrelationId(correlationId)
                .setCreatedAt(123456L)
                .setResponse(gson.toJson(TestData.populateOtpVerificationResponse(code, reason)))
                .setCode("200")
                .setCreatedBy(REST_PROXY_OTP_VERIFICATION)
                .setReplyTo("topic")
                .build();
        given(otpVerificationUtils.isPendingRequestPresent(correlationId)).willReturn(true);
        ArgumentCaptor<OtpVerificationResponse> captor = ArgumentCaptor.forClass(OtpVerificationResponse.class);

        resultTopic.pipeInput(restProxyEventResponse.getCorrelationId(), restProxyEventResponse);

        verify(otpVerificationUtils, times(1))
                .setResponse(any(), captor.capture(), eq(HttpStatus.OK));

    }

    @Test
    public void should_return_not_found_otp() {
        String correlationId = "123";
        String code = "otp_not_found";
        String reason = "The inserted OTP is not found.";

        RestProxyEventResponse restProxyEventResponse = RestProxyEventResponse.newBuilder()
                .setCorrelationId(correlationId)
                .setCreatedAt(123456L)
                .setResponse(gson.toJson(TestData.populateOtpVerificationResponse(code, reason)))
                .setCode("404")
                .setCreatedBy(REST_PROXY_OTP_VERIFICATION)
                .setReplyTo("topic")
                .build();
        given(otpVerificationUtils.isPendingRequestPresent(correlationId)).willReturn(true);
        ArgumentCaptor<OtpVerificationResponse> captor = ArgumentCaptor.forClass(OtpVerificationResponse.class);

        resultTopic.pipeInput(restProxyEventResponse.getCorrelationId(), restProxyEventResponse);

        verify(otpVerificationUtils, times(1))
                .setResponse(any(), captor.capture(), eq(HttpStatus.NOT_FOUND));
    }


}