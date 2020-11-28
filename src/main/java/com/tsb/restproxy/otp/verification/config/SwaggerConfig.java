package com.tsb.restproxy.otp.verification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    public static final Contact DEFAULT_CONTACT = new Contact("TSB Bank", "https://www.tsb.co.uk", "");
    public static final ApiInfo DEFAULT_API_INFO =
            new ApiInfo("Otp Verification",
                   "This is a Swagger contract to validate the user information provided through mobile apps.\n" +
                             "All request coming from mobile apps for regitering customer device will be handled by this component.",
                      "1.0",
                    "urn:tos",
            DEFAULT_CONTACT, "",
                    "",
                    new ArrayList<VendorExtension>());

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(DEFAULT_API_INFO);
    }
}
