package com.abtasty.flagship.api;

import com.abtasty.flagship.main.Flagship;
import com.abtasty.flagship.utils.FlagshipLogManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.*;


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

    private final ThreadPoolExecutor    threadPoolExecutor;
    private final long                  workerTimeout = 500L;
    private final TimeUnit              workerTimeoutUnit = TimeUnit.MILLISECONDS;
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
        this.threadPoolExecutor.allowCoreThreadTimeOut(true);
        ready = true;
    }

    public boolean isReady() {
        return ready;
    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
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
        long timer = System.currentTimeMillis();
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
        response.setResponseTime(System.currentTimeMillis() - timer);
        conn.disconnect();
        return response;
    }

    public CompletableFuture<Response> sendAsyncHttpRequest(RequestType type,
                                                                   String uri,
                                                                   HashMap<String, String> headers,
                                                                   String content) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendHttpRequest(type, uri, headers, content);
            } catch (IOException e) {
                FlagshipLogManager.exception(e);
            }
            return null;
        }, threadPoolExecutor);
    }

    public Response parseResponse(HttpURLConnection conn, RequestType requestType, String requestUri,
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
        Response response = new Response(status, content.toString(), conn.getResponseMessage(), headers);
        response.setRequestHeaders(requestHeaders);
        response.setRequestUrl(requestUri);
        response.setRequestContent(requestContent);
        response.setType(requestType);
        return response;
    }
}