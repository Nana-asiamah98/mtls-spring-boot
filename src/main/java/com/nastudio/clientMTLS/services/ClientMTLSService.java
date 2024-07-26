package com.nastudio.clientMTLS.services;


import com.nastudio.clientMTLS.config.AppConfig;
import com.nastudio.clientMTLS.dto.doppler.CreateDopplerSecret;
import com.nastudio.clientMTLS.dto.requests.CallbackConfigRequest;
import com.nastudio.clientMTLS.dto.responses.DataApiResponse;
import com.nastudio.clientMTLS.exception.CertificateNotExistException;
import com.nastudio.clientMTLS.repo.ClientMTLSRepo;
import com.nastudio.clientMTLS.helper.DopplerHelper;
import com.nastudio.clientMTLS.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

import static com.nastudio.clientMTLS.utils.AppConstants.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class ClientMTLSService {


    private final ClientMTLSRepo clientMTLSRepo;
    private final DopplerHelper dopplerHelper;
    private final AppConfig appConfig;


    public DataApiResponse createConfiguration(CallbackConfigRequest configRequest) throws IOException, CertificateException, URISyntaxException, KeyStoreException {


        // Validate Certificate

        // Check Options - Client or Server
        if (configRequest.getCertificateOption().equalsIgnoreCase(CLIENT_CERTIFICATE_OPTION)) {
            // Store Clients Information

            if (Objects.requireNonNull(configRequest.getCertificate().getFilename()).isEmpty() && Objects.requireNonNull(configRequest.getPrivateKey().getFilename()).isEmpty()) {
                log.error("Certificate Or Private Key Does Not Exist!!!");
                throw new CertificateNotExistException("Certificate Or Private Key Wasn't Uploaded");
            }

            // Convert File To Base64Encoded.
            String clientCertificateInfo = Base64.getEncoder().encodeToString(configRequest.getCertificate().getContentAsByteArray());
            String clientPrivateKey = Base64.getEncoder().encodeToString(configRequest.getPrivateKey().getContentAsByteArray());

            // 1. Expiration Date
            if (!validateCertificate(configRequest.getCertificate()) && isValidCertificateFile(configRequest.getCertificate())) {
                // Formulate DopplerRequestBody
                Map<String, Object> dopplerSecretMappings = new HashMap<>();
                dopplerSecretMappings.put(configRequest.getClientCode().concat(DOPPLER_PUBLIC_KEY).toUpperCase(), clientCertificateInfo);
                dopplerSecretMappings.put(configRequest.getClientCode().concat(DOPPLER_PRIVATE_KEY).toUpperCase(), clientPrivateKey);

                return saveCertificateInfo(configRequest, dopplerSecretMappings);
            }


        } else if (configRequest.getCertificateOption().equalsIgnoreCase(SERVER_CERTIFICATE_OPTION)) {
            // Store Merchant's Server CA certificate.
            if (Objects.requireNonNull(configRequest.getCertificate().getFilename()).isEmpty()) {
                log.error("Certificate Does Not Exist!!!");
                throw new CertificateNotExistException("Certificate Wasn't Uploaded");
            }

            // 1. Expiration Date
            if (!validateCertificate(configRequest.getCertificate()) && isValidCertificateFile(configRequest.getCertificate())) {

                // Convert File To Base64Encoded.
                String serverCertificateInfo = Base64.getEncoder().encodeToString(configRequest.getCertificate().getContentAsByteArray());

                // Formulate DopplerRequestBody
                Map<String, Object> dopplerSecretMappings = Map.of(
                        configRequest.getClientCode().concat(AppConstants.DOPPLER_SERVER_PUBLIC_KEY).toUpperCase(), serverCertificateInfo
                );

                return saveCertificateInfo(configRequest, dopplerSecretMappings);
            }

            throw new BadRequestException("Failed To Process Certificate!!");

        }
        throw new BadRequestException("Provide Valid Certificate Option. Either CLIENT or SERVER");

    }


    private DataApiResponse saveCertificateInfo(CallbackConfigRequest configRequest, Map<String, Object> dopplerSecretMappings) throws IOException, URISyntaxException {

        var formulatedPayload = CreateDopplerSecret
                .builder()
                .config(appConfig.getDoppler().getConfigurationName())
                .project(appConfig.getDoppler().getProjectName())
                .secrets(dopplerSecretMappings)
                .build();

        // Persist In Doppler
        var savedDopplerSecrets = dopplerHelper.createSecret(formulatedPayload, configRequest.getClientCode());

        if (savedDopplerSecrets.isSuccess()) {
            // Saving a request
            var entity = CallbackConfigRequest.toEntity(configRequest);
            var savedEntity = clientMTLSRepo.save(entity);
            return DataApiResponse
                    .builder()
                    .data(savedEntity)
                    .message("Saved Configuration")
                    .statusCode(HttpStatus.OK.value())
                    .build();
        }
        throw new BadRequestException("Failed To Store Secrets In Doppler And DB!!");
    }

    private boolean validateCertificate(Resource resource) throws CertificateException, IOException {

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate caCert = (X509Certificate) certificateFactory.generateCertificate(resource.getInputStream());

        Date dateExpiration = caCert.getNotAfter();
        var res = dateExpiration.before(new Date());

        if (res) {
            throw new BadRequestException(String.format("Certificate Date :: %s && Certificate Has Expired!!", dateExpiration));
        }

        return false;
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
    }

    private static boolean isValidCertificateFile(Resource file) throws IOException, KeyStoreException {
        log.info("[FILE] :: {}", file.getFilename());
        String fileName = file.getFilename();
        if (fileName.isEmpty()) {
            throw new BadRequestException("File Doesn't Exists.");
        }

        String extension = getFileExtension(fileName);
        if (ALLOWED_EXTENSIONS.contains(extension)) {
            return true;
        }
        throw new BadRequestException("Wrong Certificate File Extension.");
    }


}
