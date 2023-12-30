package ru.jamsys.http;

import lombok.Getter;
import lombok.Setter;
import ru.jamsys.virtual.file.system.view.FileViewKeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpClientImpl implements HttpClient {

    @Getter
    @Setter
    private String SslContextType = "TLS";

    @Getter
    @Setter
    private Proxy proxy = null;

    @Getter
    @Setter
    private int connectTimeoutMillis = 10000;

    @Getter
    @Setter
    private int readTimeoutMillis = 10000;

    @Getter
    @Setter
    public boolean checkServerTrusted = false;

    @Getter
    @Setter
    public boolean disableHostnameVerification = false;

    @Getter
    @Setter
    public String method = null;
    private final Map<String, String> headersRequest = new HashMap<>();

    @Getter
    private Map<String, List<String>> headerResponse = null;

    @Setter
    private FileViewKeyStore keyStore = null;

    @Getter
    @Setter
    private byte[] postData = null;

    @Getter
    @Setter
    private String url = null;

    @Getter
    private byte[] response = null;

    @Getter
    private int status = -1;

    @Getter
    private Exception exception = null;

    public void setRequestHeader(String name, String value) {
        headersRequest.put(name, value);
    }

    @SuppressWarnings("unused")
    public void exec() {
        try {
            URL url = new URL(this.url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) (proxy == null ? url.openConnection() : url.openConnection(proxy));

            if (httpURLConnection instanceof HttpsURLConnection) {
                configureSsl((HttpsURLConnection) httpURLConnection);
            }

            httpURLConnection.setDoOutput(true);
            httpURLConnection.setConnectTimeout(connectTimeoutMillis);
            httpURLConnection.setReadTimeout(readTimeoutMillis);
            httpURLConnection.setUseCaches(false);

            for (Map.Entry<String, String> x : headersRequest.entrySet()) {
                httpURLConnection.setRequestProperty(x.getKey(), x.getValue());
            }
            if (method == null) {
                method = postData != null ? "POST" : "GET";
            }
            httpURLConnection.setRequestMethod(method);
            if (postData != null) {
                httpURLConnection.setDoOutput(true);
            }

            httpURLConnection.connect();
            if (postData != null) {
                OutputStream out = httpURLConnection.getOutputStream();
                out.write(postData);
                out.flush();
                out.close();
            }
            InputStream inputStream = getResult(httpURLConnection);
            status = httpURLConnection.getResponseCode();
            response = read(inputStream);
            inputStream.close();
        } catch (Exception e) {
            exception = e;
            e.printStackTrace();
        }
    }

    private InputStream getResult(HttpURLConnection httpURLConnection) {
        try {
            this.headerResponse = httpURLConnection.getHeaderFields();
            return httpURLConnection.getInputStream();
        } catch (Exception e) {
            return httpURLConnection.getErrorStream();
        }
    }

    private void configureSsl(HttpsURLConnection httpsURLConnection) {
        SSLSocketFactory sslSocketFactory = null;
        if (keyStore != null) {
            sslSocketFactory = keyStore.getSslSocketFactory(SslContextType);
        }
        if (sslSocketFactory == null) { //Если хранилище из файла не загрузилось, то может вернуть null
            sslSocketFactory = SslSocketFactoryCache.getSslSocketFactory(SslContextType);
        }
        httpsURLConnection.setSSLSocketFactory(sslSocketFactory);
        if (disableHostnameVerification || !checkServerTrusted) {
            httpsURLConnection.setHostnameVerifier(TrustManager.getHostnameVerifier());
        }
    }

    private static byte[] read(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (true) {
            int data = inputStream.read(buf);
            if (data <= 0) {
                break;
            }
            out.write(buf, 0, data);
        }
        return out.toByteArray();
    }

    @SuppressWarnings("unused")
    public String getResponseString(String charset) throws UnsupportedEncodingException {
        return new String(response, charset);
    }

}