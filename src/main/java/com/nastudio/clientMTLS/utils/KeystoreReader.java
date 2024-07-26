package com.nastudio.clientMTLS.utils;


import com.nastudio.clientMTLS.config.AppConfig;
import com.nastudio.clientMTLS.dto.others.SSLKeysDto;
import com.nastudio.clientMTLS.exception.KeyStoreReaderException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.utils.Base64;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;

@Slf4j
public class KeystoreReader {

    public static File getKeyStoreWithUploadedSslCertsAndKey(SSLKeysDto sslKeysDto,
                                                             AppConfig appConfig) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
//        try {

            log.info("Starting Method:getKeyStoreWithUploadedCerts");
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null);

            //upload client  ssl cert
            X509Certificate x509Certificate = getCert(sslKeysDto.getClientSslCert());
            keyStore.setCertificateEntry(sslKeysDto.getClientAliasName(), x509Certificate);

            PrivateKey privateKey = KeystoreReader.getPKCS1EncPrivateKey(sslKeysDto.getClientPrivateKey(), "RSA", "qwerty");
            keyStore.setKeyEntry(sslKeysDto.getClientAliasName() + "_private", privateKey,
                    appConfig.getClient().getPassword().toCharArray(),
                    new X509Certificate[]{x509Certificate});

            // Absa_GH
            //upload server  ssl cert
            X509Certificate serverX509Certificate = getCert(sslKeysDto.getServerSslCert());
            keyStore.setCertificateEntry(sslKeysDto.getServerAliasName(), serverX509Certificate);

            // Create a temporary file
            File file = ResourceUtils.getFile("classpath:store.jks");

            log.info("Temp file : {}" + file);

            keyStore.store(new FileOutputStream(file), sslKeysDto.getKeyStorePassword().toCharArray());
            return file;

//        }
//        catch (Exception e) {
//            log.error("getting error on loading client and server ssl keys on key store ...", e);
//            throw new KeyStoreReaderException(e.getMessage());
//        }

    }

    public static PrivateKey getPKCS1PrivateKey(String pkB, String algorithm) {


        PrivateKey privateKey = null;
        try {

            InputStream targetStream = new ByteArrayInputStream(pkB.getBytes());

            PemReader reader    = new PemReader(new InputStreamReader(new BufferedInputStream(targetStream)));
            PemObject pemObject = reader.readPemObject();
            byte[]    content   = pemObject.getContent();
            reader.close();

            if(ASN1Primitive.fromByteArray(content) instanceof DLSequence){
                log.info("Instance of DLSequence");
            }

            var asn1InputStream = ASN1Sequence.fromByteArray(content);
//            ASN1Primitive asn1Primitive =  asn1InputStream.readObject();
            RSAPrivateKey asn1PrivKey       = RSAPrivateKey.getInstance(ASN1Sequence.fromByteArray(content));
            RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(asn1PrivKey.getModulus(),
                    asn1PrivKey.getPrivateExponent());

//            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(content,"RSA");
            KeyFactory kf                = KeyFactory.getInstance(algorithm);
            privateKey = kf.generatePrivate(rsaPrivateKeySpec);
        }
        catch (NoSuchAlgorithmException e) {
            log.error("Could not reconstruct the private key, the given algorithm could not be found.", e);
            throw new KeyStoreReaderException(e.getMessage());

        }
        catch (InvalidKeySpecException e) {
            log.error("Could not reconstruct the private key.", e);
            throw new KeyStoreReaderException(e.getMessage());

        }
        catch (IOException e) {
            log.error("Error :: ", e);
            throw new KeyStoreReaderException(e.getMessage());
        }

        return privateKey;
    }


    public static X509Certificate getCert(String publicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, CertificateException {

        byte[]               keyBytes        = Base64.decodeBase64(
                publicKey.replaceAll("-----BEGIN CERTIFICATE-----", "").replaceAll("-----END CERTIFICATE-----", "").replace("\n", ""));
        ByteArrayInputStream keyStream       = new ByteArrayInputStream(keyBytes);
        CertificateFactory certFactory     = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certFactory.generateCertificate(keyStream);
    }


    public static PrivateKey getPKCS1EncPrivateKey(String pkB, String algorithm , String passPhrase) {
        PrivateKey privateKey = null;
        try {

            byte[]               keyBytes        = Base64.decodeBase64(
                    pkB.replaceAll("-----BEGIN ENCRYPTED PRIVATE KEY-----", "").replaceAll("-----END ENCRYPTED PRIVATE KEY-----", "").replace("\n", ""));
            InputStream targetStream = new ByteArrayInputStream(pkB.getBytes());
            PEMParser keyReader = new PEMParser(new InputStreamReader(new BufferedInputStream(targetStream)));
            Object keyPair = keyReader.readObject();
            keyReader.close();

            if (keyPair instanceof PEMEncryptedKeyPair) {
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                PEMDecryptorProvider decryptionProv = new JcePEMDecryptorProviderBuilder()
                        .build(passPhrase.toCharArray());

                Security.addProvider(new BouncyCastleProvider());
                PEMKeyPair decryptedKeyPair = ((PEMEncryptedKeyPair) keyPair).decryptKeyPair(decryptionProv);
                privateKey = converter.getPrivateKey(decryptedKeyPair.getPrivateKeyInfo());
            } else if (keyPair instanceof PKCS8EncryptedPrivateKeyInfo) {

                PKCS8EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new PKCS8EncryptedPrivateKeyInfo(keyBytes);
                InputDecryptorProvider decryptorProvider = new JceOpenSSLPKCS8DecryptorProviderBuilder()
                        .setProvider(new BouncyCastleProvider())
                        .build("qwerty".toCharArray());
                PrivateKeyInfo privateKeyInfo = encryptedPrivateKeyInfo.decryptPrivateKeyInfo(decryptorProvider);
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyInfo.getEncoded());
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                return keyFactory.generatePrivate(keySpec);
            } else if (keyPair instanceof PrivateKeyInfo) {

                JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                return converter.getPrivateKey((PrivateKeyInfo) keyPair);
            } else
            {
                privateKey = getPKCS1PrivateKey(pkB , algorithm);
            }
        } catch (IOException e) {
            log.error(e.getMessage() , e);
            throw new KeyStoreReaderException(e.getMessage());
        } catch (OperatorCreationException e) {
            throw new RuntimeException(e);
        } catch (PKCSException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        return privateKey;
    }

}
