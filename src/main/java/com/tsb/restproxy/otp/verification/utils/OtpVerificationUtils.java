package com.tsb.restproxy.otp.verification.utils;

import com.tsb.ob.restproxy.commons.utils.TimedMap;
import com.tsb.restproxy.otp.verification.dto.OtpVerificationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ConcurrentLinkedQueue;


@Component
public class OtpVerificationUtils {

	public static TimedMap<String, DeferredResult> PENDING_REQUEST_CACHE;
	public static ConcurrentLinkedQueue<String> ID_QUEUE;

	static {
		ID_QUEUE = new ConcurrentLinkedQueue<>();
		PENDING_REQUEST_CACHE = new TimedMap<>();
	}

	public boolean isPendingRequestPresent(String correlationId) {
		return PENDING_REQUEST_CACHE.containsKey(correlationId);
	}

	public void setResponse(String correlationId, OtpVerificationResponse otpVerificationResponse, HttpStatus httpStatus) {
		PENDING_REQUEST_CACHE.get(correlationId).setResult(ResponseEntity.status(httpStatus).body(otpVerificationResponse));
		PENDING_REQUEST_CACHE.remove(correlationId);
	}
}
