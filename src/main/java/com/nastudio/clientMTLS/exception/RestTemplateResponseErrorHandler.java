package com.nastudio.clientMTLS.exception;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Strings;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Slf4j
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return (
                response.getStatusCode().is4xxClientError()
                        || response.getStatusCode().is5xxServerError());
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {

        if (response.getStatusCode().is5xxServerError()) {
            // handle SERVER_ERROR
            log.error("::::::::::::::[5xx error response from payment merchant]::::::::::::::");

        }
        else if (response.getStatusCode().is4xxClientError()) {
            // handle CLIENT_ERROR
            log.error("::::::::::::::[4xx error response from payment merchant]::::::::::::::");
            log.error(Strings.fromByteArray(response.getBody().readAllBytes()));
        }

    }
}
