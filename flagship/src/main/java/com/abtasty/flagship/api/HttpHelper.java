package com.abtasty.flagship.api;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class HttpHelper {

    public enum RequestType {
        POST("POST"),
        GET("GET");

        public String name = "";

        RequestType(String name) {
            this.name = name;
        }
    }

    public interface IResponse {
        public void onSuccess(Response response);

        public void onFailure(Response response);

        public void onException(Exception e);
    }

    public static HttpURLConnection createConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    public static Response sendHttpRequest(RequestType type,
                                           String uri,
                                           HashMap<String, String> headers,
                                           String content) throws IOException {
        return sendHttpRequest(type, uri, headers, content, 0);
    }

    public static Response sendHttpRequest(RequestType type,
                                           String uri,
                                           HashMap<String, String> headers,
                                           String content,
                                           int timeout) throws IOException {

        URL url = new URL(uri);
        HttpURLConnection conn = createConnection(url);
        conn.setRequestMethod(type.name);
        conn.setRequestProperty("Content-Type", "application/json");
        if (timeout > 0) {
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
        }
        if (headers != null && headers.size() > 0) {
            for (HashMap.Entry<String, String> e : headers.entrySet()) {
                conn.setRequestProperty(e.getKey(), e.getValue());
            }
        }
        if (type == RequestType.POST && content != null) {
            conn.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes(content);
            out.flush();
            out.close();
        }
        Response response = parseResponse(conn, type, uri, headers, content);
        conn.disconnect();
        return response;
    }

    public static CompletableFuture<Response> sendAsyncHttpRequest(RequestType type,
                                                                   String uri,
                                                                   HashMap<String, String> headers,
                                                                   String content,
                                                                   IResponse responseCallback) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendHttpRequest(type, uri, headers, content);
            } catch (IOException e) {
                responseCallback.onException(e);
                e.printStackTrace();
            }
            return null;

        }).whenCompleteAsync((response, error) -> {
            if (responseCallback != null && response.isSuccess())
                responseCallback.onSuccess(response);
            else if (responseCallback != null && !response.isSuccess())
                responseCallback.onFailure(response);
        });
    }

    private static Response parseResponse(HttpURLConnection conn, RequestType requestType, String requestUri,
                                          HashMap<String, String> requestHeaders, String requestContent) throws IOException {
        int status = conn.getResponseCode();
        Reader streamReader = new InputStreamReader((status > 299) ? conn.getErrorStream() : conn.getInputStream());
        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        HashMap<String, String> headers = new HashMap<String, String>();
        for (String s : conn.getHeaderFields().keySet()) {
            headers.put(s, conn.getHeaderField(s));
        }
        Response response = new Response(status, content.toString(), conn.getResponseMessage(), headers);
        response.setRequestHeaders(requestHeaders);
        response.setRequestUrl(requestUri);
        response.setRequestContent(requestContent);
        response.setType(requestType);
        in.close();
        streamReader.close();
        return response;
    }
}
