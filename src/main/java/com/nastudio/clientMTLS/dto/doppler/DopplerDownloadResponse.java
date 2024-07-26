package com.nastudio.clientMTLS.dto.doppler;

import lombok.*;

import java.util.Map;



@Getter
@Builder
public class DopplerDownloadResponse  {

    @Builder.Default
    private boolean success = true;

    private int statusCode;

    private String message;

    private Map<String, Object> data;
}
