package com.abtasty.flagship.api;

import java.util.HashMap;

public class Response {

    private HttpManager.RequestType     type = HttpManager.RequestType.GET;
    private String                      requestUrl = "";
    private String                      requestContent = "";
    private HashMap<String, String>     requestHeaders = new HashMap<String, String>();

    private int                         responseCode;
    private String                      responseContent;
    private String                      responseMessage;
    private HashMap<String, String>     responseHeaders;
    private long                        responseTime = 0;


    public Response(int code, String content, String message, HashMap<String, String> headers) {
        this.responseCode = code;
        this.responseContent = content;
        this.responseMessage = message;
        this.responseHeaders = headers;
    }

    public Boolean isSuccess() {
        return (this.responseCode < 400);
    }

    public Boolean isSuccess(Boolean ignoreNoModification) {
        return (!ignoreNoModification) ? (this.responseCode < 300) : (this.responseCode < 400);
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

    public String getResponseHeader(String field) {
        return this.responseHeaders.get(field);
    }

    public HttpManager.RequestType getType() {
        return type;
    }

    public void setType(HttpManager.RequestType type) {
        this.type = type;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    @Override
    public String toString() {
        return "Response{" +
                "type=" + type +
                ", requestUrl='" + requestUrl + '\'' +
                ", requestContent='" + requestContent + '\'' +
                ", requestHeaders=" + requestHeaders +
                ", responseCode=" + responseCode +
                ", responseContent='" + responseContent + '\'' +
                ", responseMessage='" + responseMessage + '\'' +
                ", responseHeaders=" + responseHeaders +
                '}';
    }
}