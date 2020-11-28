package com.tsb.restproxy.otp.verification.config;

import com.tsb.ob.restproxy.avro.event.RestProxyEventRequest;
import com.tsb.restproxy.otp.verification.processor.OtpVerificationProcessor;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;

@Configuration
@Slf4j
@AllArgsConstructor
public class KafkaStreamConfig {

    private final OtpVerificationProcessor otpVerificationProcessor;
    private final KafkaConfigProperties kafkaConfigProperties;

    @Bean
    public CommandLineRunner stream() {
        return args -> {
            final KafkaStreams streams = new KafkaStreams(otpVerificationProcessor.getTopology(), this.getProperties());
            streams.cleanUp();
            log.debug("about to start streaming ... ");
            streams.start();
            Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        };
    }


    @Bean
    public Producer<String, RestProxyEventRequest> producer(KafkaConfigProperties kafkaConfigProperties) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigProperties.getBootstrapServer());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaConfigProperties.getApplicationId());
        props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaConfigProperties.getSchemaRegistry());
        props.put(SaslConfigs.SASL_MECHANISM, kafkaConfigProperties.getSaslMechanism());
        props.put(SECURITY_PROTOCOL_CONFIG, kafkaConfigProperties.getSecurityProtocol());
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, kafkaConfigProperties.getSslTrustStoreLocation());
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, kafkaConfigProperties.getSslTrustStorePassword());
        props.put(SaslConfigs.SASL_JAAS_CONFIG, kafkaConfigProperties.getSaslJaasConfig());
        return new KafkaProducer<>(props);
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigProperties.getBootstrapServer());
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, kafkaConfigProperties.getApplicationId());
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        properties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaConfigProperties.getSchemaRegistry());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfigProperties.getApplicationId());
        properties.put(SaslConfigs.SASL_MECHANISM, kafkaConfigProperties.getSaslMechanism());
        properties.put(StreamsConfig.SECURITY_PROTOCOL_CONFIG, kafkaConfigProperties.getSecurityProtocol());
        properties.put(SASL_JAAS_CONFIG, kafkaConfigProperties.getSaslJaasConfig());
        properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, kafkaConfigProperties.getSslTrustStoreLocation());
        properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, kafkaConfigProperties.getSslTrustStorePassword());
        return properties;
    }
}
