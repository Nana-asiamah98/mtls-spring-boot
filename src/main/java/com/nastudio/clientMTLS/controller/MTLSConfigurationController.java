package com.nastudio.clientMTLS.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nastudio.clientMTLS.dto.requests.CallbackConfigRequest;
import com.nastudio.clientMTLS.dto.responses.DataApiResponse;
import com.nastudio.clientMTLS.exception.BadRequestException;
import com.nastudio.clientMTLS.services.ClientMTLSService;
import com.nastudio.clientMTLS.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/mtls-configuration")
public class MTLSConfigurationController {

    private final ClientMTLSService clientMTLSService;
    private final ObjectMapper objectMapper;


    @PostMapping("/create")
    public ResponseEntity<DataApiResponse> createConfiguration(String request, @RequestPart("certificate") MultipartFile certificate, @RequestPart(value = "clientPrivateKey" , required = false) MultipartFile clientPrivateKey) throws CertificateException, IOException, URISyntaxException, KeyStoreException {
        CallbackConfigRequest configRequest = objectMapper.readValue(request, CallbackConfigRequest.class);
        configRequest.setCertificate(certificate.getResource());
        if (configRequest.getCertificateOption().equalsIgnoreCase(AppConstants.CLIENT_CERTIFICATE_OPTION)) {
            Objects.requireNonNull(clientPrivateKey, "Client Private Key Hasn't been Uploaded.");
            configRequest.setPrivateKey(clientPrivateKey.getResource());
        }
        var response = clientMTLSService.createConfiguration(configRequest);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<DataApiResponse> fetchResponseAPI() {
        return ResponseEntity.ok(DataApiResponse.builder().build());
    }
}
