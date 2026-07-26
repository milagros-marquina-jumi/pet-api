package com.marquina.pet_api.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CorrelationIdInterceptorTest {

    private final CorrelationIdInterceptor interceptor = new CorrelationIdInterceptor();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void propagatesTransactionIdAsOutboundHeader() throws IOException {
        MDC.put(CorrelationConstants.TRANSACTION_ID_MDC_KEY, "60cc5c22-3250-4e07-a519-a6dab99c6713");
        HttpRequest request = new MockClientHttpRequest();

        interceptor.intercept(request, new byte[0], stubExecution());

        assertThat(request.getHeaders().getFirst(CorrelationConstants.REQUEST_ID_HEADER))
                .isEqualTo("60cc5c22-3250-4e07-a519-a6dab99c6713");
    }

    @Test
    void doesNotAddHeaderWhenNoTransactionIdInScope() throws IOException {
        HttpRequest request = new MockClientHttpRequest();

        interceptor.intercept(request, new byte[0], stubExecution());

        assertThat(request.getHeaders().containsKey(CorrelationConstants.REQUEST_ID_HEADER)).isFalse();
    }

    private ClientHttpRequestExecution stubExecution() throws IOException {
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getHeaders()).thenReturn(new HttpHeaders());

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(any(HttpRequest.class), any(byte[].class))).thenReturn(response);
        return execution;
    }
}
