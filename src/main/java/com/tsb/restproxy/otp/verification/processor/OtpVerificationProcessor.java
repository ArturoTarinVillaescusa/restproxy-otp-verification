package com.tsb.restproxy.otp.verification.processor;

import com.google.gson.Gson;
import com.tsb.ob.restproxy.avro.event.RestProxyEventResponse;
import com.tsb.restproxy.otp.verification.config.KafkaConfigProperties;
import com.tsb.restproxy.otp.verification.dto.OtpVerificationResponse;
import com.tsb.restproxy.otp.verification.utils.OtpVerificationUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class OtpVerificationProcessor {

    private final KafkaConfigProperties kafkaConfigProperties;
    private final OtpVerificationUtils otpVerificationUtils;

    public Topology getTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, RestProxyEventResponse> stream = builder.stream(kafkaConfigProperties.getSource());
        this.process(stream);
        return builder.build();
    }

    public void process(KStream<String, RestProxyEventResponse> stream) {
        stream.foreach(this::processOtpVerifiedResponse);
    }

    private void processOtpVerifiedResponse(String correlationId, RestProxyEventResponse restProxyEventResponse) {
        setResult(restProxyEventResponse);
    }

    private void setResult(RestProxyEventResponse restProxyEventResponse)  {
        boolean isPending = otpVerificationUtils.isPendingRequestPresent(restProxyEventResponse.getCorrelationId());
        if(isPending) {
            log.debug("setting result for correlationId: {}", restProxyEventResponse.getCorrelationId());
            HttpStatus httpStatus = this.identifyHttpStatus(restProxyEventResponse.getCode());

            if ((HttpStatus.CREATED == httpStatus) || (HttpStatus.OK == httpStatus)) {
                otpVerificationUtils.setResponse(restProxyEventResponse.getCorrelationId(),
                                                 this.populateTokenResponse(),
                                                 httpStatus);
            } else {
                otpVerificationUtils.setResponse(restProxyEventResponse.getCorrelationId(),
                                                 this.populateErrorResponse(restProxyEventResponse),
                                                 httpStatus);
            }
        }
    }

    private HttpStatus identifyHttpStatus(String code) {
        HttpStatus httpStatus;
        switch (Integer.parseInt(code)) {
            case 200:
                httpStatus = HttpStatus.OK;
                break;
            case 400:
                httpStatus = HttpStatus.BAD_REQUEST;
                break;
            case 401:
                httpStatus = HttpStatus.UNAUTHORIZED;
                break;
            case 403:
                httpStatus = HttpStatus.FORBIDDEN;
                break;
            case 404:
                httpStatus = HttpStatus.NOT_FOUND;
                break;
            case 405:
                httpStatus = HttpStatus.METHOD_NOT_ALLOWED;
                break;
            case 406:
                httpStatus = HttpStatus.NOT_ACCEPTABLE;
                break;
            case 408:
                httpStatus = HttpStatus.REQUEST_TIMEOUT;
                break;
            case 422:
                httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
                break;
            case 500:
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
            default:
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return httpStatus;
    }

    private OtpVerificationResponse populateTokenResponse() {
        return new OtpVerificationResponse();
    }

    private OtpVerificationResponse populateErrorResponse(RestProxyEventResponse restProxyEventResponse) {
        OtpVerificationResponse response = new Gson().fromJson(restProxyEventResponse.getResponse(), OtpVerificationResponse.class);
        return new OtpVerificationResponse(response.getCode(), response.getReason());
    }

}