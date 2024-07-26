package com.nastudio.clientMTLS.dto.requests;


import com.nastudio.clientMTLS.model.ClientMTLS;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
public class CallbackConfigRequest {

    private String clientName;
    private String clientCode;
    private String callBackURL;
    private Resource certificate;
    private Resource privateKey;
    private String certificateOption;
    private String paraphrase;
    private Boolean isMTLSEnabled;

    public static ClientMTLS toEntity(CallbackConfigRequest configRequest){
        return ClientMTLS
                .builder()
                .callBackURL(configRequest.callBackURL)
                .clientCode(configRequest.clientCode)
                .dopplerName(configRequest.clientCode.concat("_DOPPLER"))
                .clientName(configRequest.clientName)
                .certificateOption(configRequest.certificateOption)
                .pemParaphrase(configRequest.paraphrase)
                .isMtlsEnabled(configRequest.isMTLSEnabled)
                .build();
    }
}
