package ru.jamsys.http;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.jamsys.virtual.file.system.view.FileViewKeyStoreSslContext;

import java.io.*;
import java.net.Proxy;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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
    public boolean disableHostnameVerification = false;

    @Getter
    @Setter
    public String method = null;
    private final Map<String, String> headersRequest = new HashMap<>();

    @Getter
    private Map<String, List<String>> headerResponse = null;

    private FileViewKeyStoreSslContext sslContext = null;

    @Getter
    @Setter
    @ToString.Exclude
    private byte[] postData = null;

    @Getter
    @Setter
    private String url = null;

    @Getter
    @ToString.Exclude
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
            if (sslContext != null) {
                clientBuilder.sslContext(sslContext.getSslContext(SslContextType));
            }

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(this.url));
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
    public String getResponseString(String charset) throws UnsupportedEncodingException {
        return new String(response, charset);
    }

    @Override
    public void setKeyStore(ru.jamsys.virtual.file.system.File keyStore, Object... props) throws Exception {
        sslContext = keyStore.getView(FileViewKeyStoreSslContext.class, props);
    }

    @SuppressWarnings("unused")
    @ToString.Include()
    public String getResponseString() {
        return response != null ? new String(response, StandardCharsets.UTF_8) : "null";
    }

    @SuppressWarnings("unused")
    @ToString.Include()
    public String getPostDataString() {
        return postData != null ? new String(postData, StandardCharsets.UTF_8) : "null";
    }

}
