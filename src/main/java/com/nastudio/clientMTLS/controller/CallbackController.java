package com.nastudio.clientMTLS.controller;


import com.nastudio.clientMTLS.dto.responses.DataApiResponse;
import com.nastudio.clientMTLS.dto.others.URLRequestDTO;
import com.nastudio.clientMTLS.services.CallbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/client-callbacks")
public class CallbackController {

    private final CallbackService callbackService;

    @PostMapping("/send-request")
    public ResponseEntity<DataApiResponse> clientResponseEntity(@RequestBody URLRequestDTO requestDTO) throws URISyntaxException {
        return ResponseEntity.ok(callbackService.getResponseFromServerMTLS(requestDTO.getClientCode()));
    }


}
