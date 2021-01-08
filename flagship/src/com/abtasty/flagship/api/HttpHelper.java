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

    @FunctionalInterface
    public interface IResponse2 {
        public void accept(String s, Integer i, Boolean b);
    }

    public interface IResponse {
        public void onSuccess(Response response);

        public void onFailure(Response response);

        public void onException(Exception e);
    }

    public static Response sendHttpRequest(RequestType type,
                                           String uri,
                                           HashMap<String, String> headers,
                                           String content) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(type.name);
        conn.setRequestProperty("Content-Type", "application/json");
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
        Response response = parseResponse(conn);
        return response;
    }


    public static void function(IResponse2 r) {
        r.accept("coucou", 3, true);
    }

    public static void sendAsyncHttpRequest(RequestType type,
                                            String uri,
                                            HashMap<String, String> headers,
                                            String content,
                                            IResponse responseCallback) {

        function((title, age, is) -> {
            System.out.println("=> " + title + " " + age + " " + is);
        });
        CompletableFuture<Response> future = CompletableFuture.supplyAsync(() -> {
            try {
                return sendHttpRequest(type, uri, headers, content);
            } catch (IOException e) {
                e.printStackTrace();
                responseCallback.onException(e);
            }
            return null;

        }).whenCompleteAsync((response, error) -> {
            if (responseCallback != null && response.isSuccess())
                responseCallback.onSuccess(response);
            else if (responseCallback != null && !response.isSuccess())
                responseCallback.onFailure(response);
        });
    }

    private static Response parseResponse(HttpURLConnection conn) throws IOException {
        int status = conn.getResponseCode();
        Reader streamReader = new InputStreamReader((status > 299) ? conn.getErrorStream() : conn.getInputStream());
        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        HashMap<String, String> headers = new HashMap();
        for (String s : conn.getHeaderFields().keySet()) {
            headers.put(s, conn.getHeaderField(s));
        }
        Response response = new Response(status, content.toString(), conn.getResponseMessage(), headers);
        in.close();
        conn.disconnect();
        return response;
    }
}
