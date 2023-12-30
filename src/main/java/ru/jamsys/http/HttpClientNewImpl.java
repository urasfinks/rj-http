package ru.jamsys.http;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ru.jamsys.virtual.file.system.view.FileViewKeyStore;

import java.io.*;
import java.net.Proxy;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class HttpClientNewImpl implements HttpClient {
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

    @Override
    public void setRequestHeader(String name, String value) {
        headersRequest.put(name, value);
    }

    @Override
    public void exec() {
        try {
            java.net.http.HttpClient.Builder clientBuilder = java.net.http.HttpClient.newBuilder();
            if (keyStore != null) {
                clientBuilder.sslContext(keyStore.getSslContext(SslContextType));
            }

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
            requestBuilder.uri(new URL(this.url).toURI());
            for (Map.Entry<String, String> x : headersRequest.entrySet()) {
                requestBuilder.setHeader(x.getKey(), x.getValue());
            }
            if (method == null) {
                method = postData != null ? "POST" : "GET";
            }
            HttpMethodEnum parseMethod = HttpMethodEnum.valueOf(method);
            switch (parseMethod) {
                case GET:
                    requestBuilder.GET();
                    break;
                case POST:
                    requestBuilder.method(method, HttpRequest.BodyPublishers.ofByteArray(postData));
                    break;
                case PUT:
                    requestBuilder.PUT(HttpRequest.BodyPublishers.ofByteArray(postData));
                    break;
                case DELETE:
                    requestBuilder.DELETE();
                    break;
            }
            requestBuilder.timeout(Duration.ofMillis(connectTimeoutMillis + readTimeoutMillis));
            HttpResponse<byte[]> responses = clientBuilder.build().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());

            status = responses.statusCode(); // Return the status code, if it is 200, it means the sending is successful
            response = responses.body();
            this.headerResponse = responses.headers().map();
        } catch (Exception e) {
            exception = e;
            e.printStackTrace();
        }
    }

    @Override
    public void setBasicAuth(String user, String pass, String charset) {

    }

    @Override
    public String getResponseString(String charset) throws UnsupportedEncodingException {
        return new String(response, charset);
    }

}
