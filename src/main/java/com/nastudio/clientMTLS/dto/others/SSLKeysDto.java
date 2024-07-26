package com.nastudio.clientMTLS.dto.others;


import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SSLKeysDto {

    private String serverSslCert;
    private String clientSslCert;
    private String keyStorePassword;
    private String serverAliasName;
    private String clientAliasName;
    private String keyStoreFileName;
    private String clientPrivateKey;
}
