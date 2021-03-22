package com.abtasty.flagship.api;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.utils.FlagshipLogManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpManager {

    private static HttpManager instance;

    private final ExecutorService httpExecutorService = Executors.newCachedThreadPool(); // todo place in its own singleton

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



    public static synchronized HttpManager getInstance() {
        if (instance == null) {
            synchronized (HttpManager.class) {
                if (instance == null)
                    instance = new HttpManager();
            }
        }
        return instance;
    }

    public HttpURLConnection createConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    public Response sendHttpRequest(RequestType type,
                                           String uri,
                                           HashMap<String, String> headers,
                                           String content) throws IOException {
        return sendHttpRequest(type, uri, headers, content, 0);
    }

    public Response sendHttpRequest(RequestType type,
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

    public CompletableFuture<Response> sendAsyncHttpRequest(RequestType type,
                                                                   String uri,
                                                                   HashMap<String, String> headers,
                                                                   String content,
                                                                   IResponse responseCallback) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendHttpRequest(type, uri, headers, content);
            } catch (IOException e) {
                responseCallback.onException(e);
                FlagshipLogManager.exception(e);
            }
            return null;

        }).whenCompleteAsync((response, error) -> {
            if (responseCallback != null && response.isSuccess())
                responseCallback.onSuccess(response);
            else if (responseCallback != null && !response.isSuccess())
                responseCallback.onFailure(response);
        });
    }

    private Response parseResponse(HttpURLConnection conn, RequestType requestType, String requestUri,
                                          HashMap<String, String> requestHeaders, String requestContent) throws IOException {

        int status = conn.getResponseCode();
        Reader streamReader = new InputStreamReader((status >= 400) ? conn.getErrorStream() : conn.getInputStream());
        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        streamReader.close();
        HashMap<String, String> headers = new HashMap<String, String>();
        for (String s : conn.getHeaderFields().keySet()) {
            headers.put(s, conn.getHeaderField(s));
        }
        String message = conn.getResponseMessage();
        Response response = new Response(status, content.toString(), message, headers);
        response.setRequestHeaders(requestHeaders);
        response.setRequestUrl(requestUri);
        response.setRequestContent(requestContent);
        response.setType(requestType);
        return response;
    }

    public void closeExecutor() {
        httpExecutorService.shutdownNow();
    }
}
