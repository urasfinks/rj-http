package ru.jamsys.http;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SslSocketFactoryCache {

    private static Map<String, SSLSocketFactory> sslSocketFactory = new ConcurrentHashMap<>();

    public static SSLSocketFactory getSslSocketFactory(String sslContextType) {
        if (!sslSocketFactory.containsKey(sslContextType)) {
            try {
                SSLContext ssl = SSLContext.getInstance(sslContextType);
                ssl.init(null, TrustManager.getListTrustManager(), new SecureRandom());
                sslSocketFactory.put(sslContextType, ssl.getSocketFactory());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sslSocketFactory.get(sslContextType);
    }

}
