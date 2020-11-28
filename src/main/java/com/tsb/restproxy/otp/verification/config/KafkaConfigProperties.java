package com.tsb.restproxy.otp.verification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka.custom.config")
@Data
public class KafkaConfigProperties {

    private String source;
    private String sink;
    private String bootstrapServer;
    private String applicationId;
    private String schemaRegistry;
    private String zooKeeperServer;
    private String sinkThrough;
    private String securityProtocol;
    private String saslJaasConfig;
    private String saslMechanism;
    private String sslTrustStoreLocation;
    private String sslTrustStorePassword;
}
