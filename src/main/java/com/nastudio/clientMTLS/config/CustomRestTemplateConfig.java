package com.nastudio.clientMTLS.config;


import com.nastudio.clientMTLS.dto.others.SSLKeysDto;
import com.nastudio.clientMTLS.exception.RestTemplateResponseErrorHandler;
import com.nastudio.clientMTLS.utils.KeystoreReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomRestTemplateConfig {


    private final AppConfig appConfig;

    public RestTemplate buildCustomRestTemplate(boolean isSSLConfigured, SSLKeysDto sslKeysDto ){
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        return  restTemplateBuilder.requestFactory(()-> {
            try {
                return getRequestFactory(isSSLConfigured , sslKeysDto);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (KeyStoreException e) {
                throw new RuntimeException(e);
            } catch (KeyManagementException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (UnrecoverableKeyException e) {
                throw new RuntimeException(e);
            } catch (CertificateException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        }).errorHandler(new RestTemplateResponseErrorHandler()).build();
    }



    private HttpComponentsClientHttpRequestFactory getRequestFactory(Boolean enabledSSL ,  SSLKeysDto sslKeysDto) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, UnrecoverableKeyException, CertificateException, InvalidKeySpecException {
        TrustStrategy trustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                return true;
            }
        };
        SSLContext sslContext = null;

        if(enabledSSL){
            log.info("SSL in enabled");
            log.info("[DOPPLER] :: {}", appConfig.getDoppler().getUrl());
            File keyStoreFile = KeystoreReader.getKeyStoreWithUploadedSslCertsAndKey(sslKeysDto,appConfig);

            try {
                sslContext = SSLContexts.custom()
                        .loadKeyMaterial(keyStoreFile, appConfig.getClient().getPassword().toCharArray(), appConfig.getClient().getPassword().toCharArray()) //Sign Key  -> Client
                        .loadTrustMaterial(keyStoreFile, appConfig.getClient().getPassword().toCharArray()) //Encrypt key -> CITI cert
                        .build();
            } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | CertificateException | IOException | UnrecoverableKeyException e) {
                log.error("getting error on creating ssl context:"+e.getMessage(),e);
            }
        }else{
            log.info("SSL in disabled");

            sslContext = SSLContexts.custom().loadTrustMaterial(null,trustStrategy).build();
        }

        SSLConnectionSocketFactory sslConFactory = new SSLConnectionSocketFactory(sslContext);

        HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslConFactory)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();

      return new HttpComponentsClientHttpRequestFactory(httpClient);
    }



}
