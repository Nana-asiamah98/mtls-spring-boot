package com.nastudio.clientMTLS.exception;

public class CertificateNotExistException extends RuntimeException{

    public CertificateNotExistException(String message) {
        super(message);
    }
}
