package com.abtasty.flagship.api;

import com.abtasty.flagship.main.Flagship;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class HttpManager {


    public enum RequestType {
        POST("POST"),
        GET("GET");

        public String name = "";

        RequestType(String name) {
            this.name = name;
        }
    }

    private static volatile HttpManager instance = null;

    private OkHttpClient                client;
    private ThreadPoolExecutor          threadPoolExecutor;
    private final long                  workerTimeout = 500L;
    private final TimeUnit              workerTimeoutUnit = TimeUnit.SECONDS;
    private int                         workers = 0;

    public static HttpManager getInstance() {
        if (instance == null) {
            synchronized (Flagship.class) {
                if (instance == null)
                    instance = new HttpManager();
            }
        }
        return instance;
    }

    private HttpManager() {
        this.client = new OkHttpClient.Builder()
//                .readTimeout(Flagship.getConfig().getTimeout())
//                .writeTimeout(Flagship.getConfig().getTimeout())
                .build();
        this.workers = Runtime.getRuntime().availableProcessors() * 2;
        this.threadPoolExecutor = new ThreadPoolExecutor(
                this.workers, this.workers,
                this.workerTimeout, this.workerTimeoutUnit,
                new LinkedBlockingQueue<Runnable>(),
                r -> {
                    Thread t = new Thread(r, "Flagship Worker");
                    t.setDaemon(true);
                    return t;
                });
    }

    public void setHttpClient(OkHttpClient client) {
        if (client != null)
            this.client = client;
    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    private Request buildRequest(RequestType type,
                                 String uri,
                                 HashMap<String, String> headers,
                                 String content){
        Request.Builder builder = new Request.Builder();
        builder.url(uri);
        builder = (type == RequestType.POST) ? builder.post(RequestBody.create(content, MediaType.get("application/json"))) : builder.get();
        if (headers != null && headers.size() > 0) {
            for (HashMap.Entry<String, String> e : headers.entrySet()) {
                builder.addHeader(e.getKey(), e.getValue());
            }
        }
        return builder.build();
    }

    public Response sendHttpRequest(RequestType type,
                                    String uri,
                                    HashMap<String, String> headers,
                                    String content) throws IOException {
        Request request = buildRequest(type, uri, headers, content);
        return client.newCall(request).execute();
    }

    public static class OKHttpCompletableFuture implements Callback {

        private final CompletableFuture<Response> completableFuture = new CompletableFuture<>();

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            completableFuture.completeExceptionally(e);
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            completableFuture.complete(response);
        }

        public CompletableFuture<Response> getFuture() {
            return completableFuture;
        }
    }

    public CompletableFuture<Response> sendAsyncHttpRequest(RequestType type,
                                                            String uri,
                                                            HashMap<String, String> headers,
                                                            String content) {
        OKHttpCompletableFuture completableFuture = new OKHttpCompletableFuture();
        Request request = buildRequest(type, uri, headers, content);
        client.newCall(request).enqueue(completableFuture);
        return completableFuture.completableFuture;
    }

//    public interface IResponse {
//        public void onSuccess(Response response);
//
//        public void onFailure(Response response);
//
//        public void onException(Exception e);
//    }

//
//    public static HttpURLConnection createConnection(URL url) throws IOException {
//        return (HttpURLConnection) url.openConnection();
//    }
//
//    public static Response sendHttpRequest(RequestType type,
//                                           String uri,
//                                           HashMap<String, String> headers,
//                                           String content) throws IOException {
//        return sendHttpRequest(type, uri, headers, content, 0);
//    }
//
//    public static Response sendHttpRequest(RequestType type,
//                                           String uri,
//                                           HashMap<String, String> headers,
//                                           String content,
//                                           int timeout) throws IOException {
//        long top = System.currentTimeMillis();
//        URL url = new URL(uri);
//        HttpURLConnection conn = createConnection(url);
//        System.out.println("REQUEST timer 0 = " + (System.currentTimeMillis() - top));
//        conn.setRequestMethod(type.name);
//        conn.setRequestProperty("Content-Type", "application/json");
//        if (timeout > 0) {
//            conn.setConnectTimeout(timeout);
//            conn.setReadTimeout(timeout);
//        }
//        if (headers != null && headers.size() > 0) {
//            for (HashMap.Entry<String, String> e : headers.entrySet()) {
//                conn.setRequestProperty(e.getKey(), e.getValue());
//            }
//        }
//        System.out.println("REQUEST timer 1 = " + (System.currentTimeMillis() - top));
//
//        if (type == RequestType.POST && content != null) {
//            conn.setDoOutput(true);
//            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
//            out.writeBytes(content);
//            out.flush();
//            out.close();
//        }
//        System.out.println("REQUEST timer 2 = " + (System.currentTimeMillis() - top));
//        Response response = parseResponse(conn, type, uri, headers, content);
//        conn.disconnect();
//        System.out.println("REQUEST timer 3 = " + (System.currentTimeMillis() - top));
//        return response;
//    }
//
//    public static CompletableFuture<Response> sendAsyncHttpRequest(RequestType type,
//                                                                   String uri,
//                                                                   HashMap<String, String> headers,
//                                                                   String content,
//                                                                   IResponse responseCallback) {
//
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                return sendHttpRequest(type, uri, headers, content);
//            } catch (IOException e) {
//                responseCallback.onException(e);
//                FlagshipLogManager.exception(e);
//            }
//            return null;
//        }).whenCompleteAsync((response, error) -> {
//            if (responseCallback != null && response.isSuccess())
//                responseCallback.onSuccess(response);
//            else if (responseCallback != null && !response.isSuccess())
//                responseCallback.onFailure(response);
//        });
//    }
//
//    private static Response parseResponse(HttpURLConnection conn, RequestType requestType, String requestUri,
//                                          HashMap<String, String> requestHeaders, String requestContent) throws IOException {
//
//        int status = conn.getResponseCode();
//        Reader streamReader = new InputStreamReader((status >= 400) ? conn.getErrorStream() : conn.getInputStream());
//        BufferedReader in = new BufferedReader(streamReader);
//        String inputLine;
//        StringBuilder content = new StringBuilder();
//        while ((inputLine = in.readLine()) != null) {
//            content.append(inputLine);
//        }
//        in.close();
//        streamReader.close();
//        HashMap<String, String> headers = new HashMap<String, String>();
//        for (String s : conn.getHeaderFields().keySet()) {
//            headers.put(s, conn.getHeaderField(s));
//        }
//        String message = conn.getResponseMessage();
//        Response response = new Response(status, content.toString(), message, headers);
//        response.setRequestHeaders(requestHeaders);
//        response.setRequestUrl(requestUri);
//        response.setRequestContent(requestContent);
//        response.setType(requestType);
//        return response;
//    }
}
