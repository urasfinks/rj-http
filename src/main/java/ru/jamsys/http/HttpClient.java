package ru.jamsys.http;

import lombok.Data;
import ru.jamsys.UtilBase64;
import ru.jamsys.virtual.file.system.view.FileViewKeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class HttpClient {

    private final String SslContextType = "TLS";
    private Proxy proxy = null;
    private int connectTimeoutMillis = 10000;
    private int readTimeoutMillis = 10000;
    public boolean checkServerTrusted = false;
    public boolean disableHostnameVerification = false;
    public String method = null;
    private final Map<String, String> headersRequest = new HashMap<>();
    private Map<String, List<String>> headerResponse = null;
    private FileViewKeyStore fileViewKeyStore = null;
    private byte[] postData = null;
    private String url = null;
    private byte[] response = null;
    private int status = -1;
    private Exception exception = null;

    public void setRequestHeader(String name, String value) {
        headersRequest.put(name, value);
    }

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
        if (fileViewKeyStore != null) {
            sslSocketFactory = fileViewKeyStore.getSslSocketFactory(SslContextType);
        }
        if (sslSocketFactory == null) {
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

    public void setBasicAuth(String user, String pass, String charset) {
        setRequestHeader("Authorization", "Basic " + UtilBase64.base64Encode(user + ":" + pass, charset, false));
    }

    public String getResponseString(String charset) throws UnsupportedEncodingException {
        return new String(response, charset);
    }

    public void setProxy(Proxy.Type type, String hostname, int port) {
        setProxy(new Proxy(type, new InetSocketAddress(hostname, port)));
    }

    public void setProxy(String hostname, int port) {
        setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port)));
    }

}