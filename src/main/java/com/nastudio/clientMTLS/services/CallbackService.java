/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.nastudio.clientMTLS.services;


import com.nastudio.clientMTLS.config.AppConfig;
import com.nastudio.clientMTLS.config.CustomRestTemplateConfig;
import com.nastudio.clientMTLS.dto.doppler.DopplerDownloadResponse;
import com.nastudio.clientMTLS.dto.others.SSLKeysDto;
import com.nastudio.clientMTLS.dto.responses.DataApiResponse;
import com.nastudio.clientMTLS.helper.DopplerHelper;
import com.nastudio.clientMTLS.model.ClientMTLS;
import com.nastudio.clientMTLS.repo.ClientMTLSRepo;
import com.nastudio.clientMTLS.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Strings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;




/*
*
*  This service makes a call to the Callback Service
* */

@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackService {

    private final ClientMTLSRepo clientMTLSRepo;
    private final CustomRestTemplateConfig restTemplate;
    private final AppConfig appConfig;
    private final DopplerHelper dopplerHelper;


    public DataApiResponse getResponseFromServerMTLS(String clientCode) throws URISyntaxException {

        Optional<ClientMTLS> config = clientMTLSRepo.findDistinctByClientCode(clientCode);

        if(config.isPresent()){
            var mtlsConfig = config.get();

            DopplerDownloadResponse dataApiResponse =  dopplerHelper.fetchSecret(clientCode);
            var res = dataApiResponse.getData();


            if(dataApiResponse.isSuccess()){
                log.info("[DOPPLER VALUE EXISTS FOR CLIENT {} ", mtlsConfig.getClientCode().toUpperCase());


                // Client  - Cellulant
                // Server - ABSA_GH
                String clientPrivateKey = (String) Objects.requireNonNull(res.get(appConfig.getClient().getClientName().concat(AppConstants.DOPPLER_PRIVATE_KEY).toUpperCase()), "Client Private Key Doesn't Exists!");
                String clientPublicKey = (String) Objects.requireNonNull(res.get(appConfig.getClient().getClientName().concat(AppConstants.DOPPLER_PUBLIC_KEY).toUpperCase()), "Client Certificate  Doesn't Exists!!");
                String serverPublicKey = (String) Objects.requireNonNull(res.get(clientCode.concat(AppConstants.DOPPLER_SERVER_PUBLIC_KEY).toUpperCase()),"Server Certificate Doesn't Exists!!");


                /*log.info("[CLIENT PRIVATE KEY] :: {}" , clientPrivateKey);
                log.info("[CLIENT PUBLIC KEY] :: {}" , clientPublicKey);
                log.info("[SERVER PUBLIC KEY] :: {}" , serverPublicKey);*/

                // Formulating of the SSLKeys to be stored within the truststore
                // Make the request to merchant's server

                SSLKeysDto sslKeysDto=
                        SSLKeysDto.builder()
                                .serverSslCert(decodeBase64Certificate(serverPublicKey))
                                .clientSslCert( decodeBase64Certificate(clientPublicKey))
                                .keyStorePassword(appConfig.getClient().getPassword())
                                .serverAliasName(mtlsConfig.getClientName())
                                .clientAliasName(appConfig.getClient().getAliasName())
                                .keyStoreFileName(appConfig.getClient().getKeyStoreFileName())
                                .clientPrivateKey(decodeBase64Certificate(clientPrivateKey)).build();

                // Make Request To MTLS Server
                ResponseEntity<Map<String, Object>> mtlsResponse = restTemplate
                        .buildCustomRestTemplate(mtlsConfig.getIsMtlsEnabled(), sslKeysDto)
                        .exchange(mtlsConfig.getCallBackURL(), HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {
                        });

                if(mtlsResponse.getStatusCode().is2xxSuccessful()){

                    log.info("[Response] :: {}", mtlsResponse);
                    return DataApiResponse.
                            builder()
                            .message(HttpStatus.OK.name())
                            .statusCode(HttpStatus.OK.value())
                            .data(mtlsResponse.getBody())
                            .build();
                }else{
                    return DataApiResponse.
                            builder()
                            .message(mtlsResponse.getStatusCode().toString())
                            .statusCode(mtlsResponse.getStatusCode().value())
                            .build();
                }


            }else {
                return DataApiResponse.
                        builder()
                        .success(false)
                        .message(HttpStatus.NOT_FOUND.name())
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .data("Doppler Value(s) Doesn't Exists.")
                        .build();
            }


        }else{
            return DataApiResponse.
                    builder()
                    .success(false)
                    .message(HttpStatus.NOT_FOUND.name())
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .build();
        }

    }

    private static String decodeBase64Certificate(String encodedBase64Script){
        return Strings.fromByteArray(Base64.getDecoder().decode(encodedBase64Script));
    }

}
