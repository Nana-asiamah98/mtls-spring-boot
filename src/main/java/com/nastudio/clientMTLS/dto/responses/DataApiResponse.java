package com.nastudio.clientMTLS.dto.responses;


import lombok.*;

@Builder
@Data
public class DataApiResponse {

    @Builder.Default
    private boolean success = true;

    private int statusCode;

    private String message;

    private Object data;
}
