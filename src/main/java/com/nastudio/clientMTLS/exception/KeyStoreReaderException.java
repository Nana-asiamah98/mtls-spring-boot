package com.nastudio.clientMTLS.exception;

import java.io.Serializable;

public class KeyStoreReaderException extends  RuntimeException implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public
    KeyStoreReaderException(String message) {
        super(message);
    }
}
