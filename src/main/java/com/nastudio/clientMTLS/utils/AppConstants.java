package com.nastudio.clientMTLS.utils;

import java.util.Arrays;
import java.util.List;

public interface AppConstants {

     static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("crt", "cer", "pem");


    static final String DOPPLER_PRIVATE_KEY = "_CLIENT_PRIVATE_KEY_DOPPLER";
    static final String DOPPLER_PUBLIC_KEY = "_CLIENT_PUBLIC_KEY_DOPPLER";
    static final String DOPPLER_SERVER_PUBLIC_KEY = "_SERVER_PUBLIC_KEY_DOPPLER";



    /* [START] - Certificate Option*/
    static final String CLIENT_CERTIFICATE_OPTION = "CLIENT";
    static final String SERVER_CERTIFICATE_OPTION = "SERVER";
    /* [END] - Certificate Option*/
}
