package com.nastudio.clientMTLS.config;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Data
@Configuration
@ConfigurationProperties("app")
public class AppConfig {

    private Doppler doppler;
    private Client client;


    /* Start - Client Configuration */
    @Data
    public static class Client{
        private Resource clientPublicKey;
        private Resource clientPrivateKey;
        private Resource serverPubicKey;
        private Resource serverPrivateKey;
        private String password;
        private String aliasName;
        private String keyStoreFileName;
        private String clientName;
    }
    /* End - Client Configuration */

    /* Start - Doppler Configs*/
    @Data
    public static class Doppler{
        private String url;
        private String token;
        private String projectName;
        private String configurationName;
    }
    /* End - Doppler Configs*/

}
