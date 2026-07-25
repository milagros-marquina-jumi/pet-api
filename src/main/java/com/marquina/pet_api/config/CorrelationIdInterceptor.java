package com.marquina.pet_api.config;

import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CorrelationIdInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        String transactionId = MDC.get(CorrelationConstants.TRANSACTION_ID_MDC_KEY);
        if (transactionId != null) {
            request.getHeaders().add(CorrelationConstants.REQUEST_ID_HEADER, transactionId);
        }

        return execution.execute(request, body);
    }
}
