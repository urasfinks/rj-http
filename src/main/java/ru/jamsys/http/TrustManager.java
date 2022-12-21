package ru.jamsys.http;

import lombok.Getter;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

public class TrustManager {

    private static X509TrustManager trustManager = new X509TrustManager() {
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };

    @Getter
    private static X509TrustManager[] listTrustManager = {trustManager};

    @Getter
    private static HostnameVerifier hostnameVerifier = (hostname, session) -> true;

}
