package com.tsb.restproxy.otp.verification.publishers;


import com.tsb.ob.restproxy.avro.event.RestProxyEventRequest;
import com.tsb.restproxy.otp.verification.config.KafkaConfigProperties;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OtpVerificationPublisher {

    private final Producer<String, RestProxyEventRequest> producer;
    private final KafkaConfigProperties kafkaConfigProperties;

    public void sendAuthEventRequestToKafka(RestProxyEventRequest restProxyEventRequest) {
        ProducerRecord<String, RestProxyEventRequest> producerRecord =
                new ProducerRecord<>(kafkaConfigProperties.getSink() , restProxyEventRequest.getUserId(), restProxyEventRequest);
        producer.send(producerRecord);
    }
}
