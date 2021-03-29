package com.abtasty.flagship.api;

import com.abtasty.flagship.main.Flagship;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
    private final ThreadPoolExecutor    threadPoolExecutor;
    private final long                  workerTimeout = 500L;
    private final TimeUnit              workerTimeoutUnit = TimeUnit.SECONDS;
    private int                         workers = 0;
    private boolean                     ready = false;

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
                .cache(null)
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
        ready = true;

    }

    public void setHttpClient(OkHttpClient client) {
        if (client != null)
            this.client = client;
    }

    public boolean isReady() {
        return ready;
    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    private Request buildRequest(RequestType type,
                                 String uri,
                                 HashMap<String, String> headers,
                                 String content) {
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
}