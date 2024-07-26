package com.nastudio.clientMTLS.dto.doppler;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Builder
@Getter
@Setter
public class CreateDopplerSecret {
    private String project;
    private String config;
    private Map<String, Object> secrets;
    private String note;
}
