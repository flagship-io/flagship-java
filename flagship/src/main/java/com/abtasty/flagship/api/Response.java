package com.abtasty.flagship.api;

import java.util.HashMap;

public class Response {

    public HttpHelper.RequestType type = HttpHelper.RequestType.GET;
    public String requestUrl = "";
    public String requestContent = "";
    public HashMap<String, String> requestHeaders = new HashMap();

    public int responseCode = -1;
    public String responseContent = "";
    public String responseMessage = "";
    public HashMap<String, String> responseHeaders = new HashMap();

    public Response(int code, String content, String message, HashMap<String, String> headers) {
        this.responseCode = code;
        this.responseContent = content;
        this.responseMessage = message;
        this.responseHeaders = headers;
    }

    public Boolean isSuccess() {
        return (this.responseCode < 300);
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestContent() {
        return requestContent;
    }

    public void setRequestContent(String requestContent) {
        this.requestContent = requestContent;
    }

    public HashMap<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(HashMap<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public HashMap<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(HashMap<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getResponseheader(String field) {
        return this.responseHeaders.get(field);
    }

    public HttpHelper.RequestType getType() {
        return type;
    }

    public void setType(HttpHelper.RequestType type) {
        this.type = type;
    }
}