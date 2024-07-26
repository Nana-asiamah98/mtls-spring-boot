package com.nastudio.clientMTLS.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nastudio.clientMTLS.config.AppConfig;
import com.nastudio.clientMTLS.config.CustomRestTemplateConfig;
import com.nastudio.clientMTLS.config.DopplerRoutes;
import com.nastudio.clientMTLS.dto.doppler.CreateDopplerSecret;
import com.nastudio.clientMTLS.dto.doppler.DopplerDownloadResponse;
import com.nastudio.clientMTLS.dto.others.SSLKeysDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DopplerHelper {


    private final CustomRestTemplateConfig restTemplate;
    private final AppConfig appConfig;


    public DopplerDownloadResponse createSecret(CreateDopplerSecret dopplerSecret, String clientCode) throws URISyntaxException, JsonProcessingException {

        log.info("Creating A Doppler Secret For Client :: {}", clientCode);

        URIBuilder uriBuilder = new URIBuilder(appConfig.getDoppler().getUrl().concat(DopplerRoutes.CREATE_SECRET));

        log.info("[DOPPLER URL] :: {}",uriBuilder.toString() );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(appConfig.getDoppler().getToken());

        HttpEntity<CreateDopplerSecret> httpEntity = new HttpEntity<CreateDopplerSecret>(dopplerSecret,httpHeaders);
        ResponseEntity<Map<String, Object>> response = restTemplate
                .buildCustomRestTemplate(false, SSLKeysDto.builder().build())
                .exchange(uriBuilder.toString(), HttpMethod.POST, httpEntity, new ParameterizedTypeReference<Map<String, Object>>() {
                });

        if(response.getStatusCode().is2xxSuccessful()){
            log.info("Created A Doppler Secret For Client :: {}", clientCode);

            var finalResponseBody = response.getBody();
            return DopplerDownloadResponse
                    .builder()
                    .data(finalResponseBody)
                    .message(response.getStatusCode().toString())
                    .statusCode(response.getStatusCode().value())
                    .build();
        }
        else {
            return DopplerDownloadResponse
                    .builder()
                    .success(false)
                    .message(response.getStatusCode().toString())
                    .statusCode(response.getStatusCode().value())
                    .build();
        }
    }

    public DopplerDownloadResponse fetchSecret(String clientCode) throws URISyntaxException {
        log.info("[FETCHING CLIENT DOPPLER VALUE: {}", clientCode);

        URIBuilder uriBuilder = new URIBuilder(appConfig.getDoppler().getUrl().concat(DopplerRoutes.FETCH_SECRET));
        uriBuilder.addParameter("project", appConfig.getDoppler().getProjectName());
        uriBuilder.addParameter("config", appConfig.getDoppler().getConfigurationName());


        log.info("[DOPPLER URL] :: {}",uriBuilder.toString() );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(appConfig.getDoppler().getToken());

        HttpEntity<Void> httpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<Map<String, Object>> response = restTemplate
                .buildCustomRestTemplate(false, SSLKeysDto.builder().build())
                .exchange(uriBuilder.toString(), HttpMethod.GET, httpEntity, new ParameterizedTypeReference<Map<String, Object>>() {
                });

        if(response.getStatusCode().is2xxSuccessful()){
            var finalResponseBody = response.getBody();
//            log.info("[Doppler Response Body] :: {}", finalResponseBody.get(dopplerKey));
            return DopplerDownloadResponse
                    .builder()
                    .data(finalResponseBody)
                    .message(response.getStatusCode().toString())
                    .statusCode(response.getStatusCode().value())
                    .build();
        }
        else {
            return DopplerDownloadResponse
                    .builder()
                    .message(response.getStatusCode().toString())
                    .statusCode(response.getStatusCode().value())
                    .build();
        }
    }


}
