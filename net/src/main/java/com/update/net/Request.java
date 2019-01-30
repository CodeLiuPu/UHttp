package com.update.net;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : liupu.
 * @date : 2019/1/22.
 */
public class Request {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    private Map<String, String> headers;
    private String method;
    private HttpUrl httpUrl;
    private RequestBody requestBody;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public HttpUrl getHttpUrl() {
        return httpUrl;
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    public String getMethod() {
        return method;
    }

    public Request(Builder builder) {
        this.headers = builder.headers;
        this.method = builder.method;
        this.httpUrl = builder.httpUrl;
        this.requestBody = builder.requestBody;
    }

    public static final class Builder {
        private Map<String, String> headers = new HashMap<>();
        private String method = METHOD_GET;
        private HttpUrl httpUrl;
        private RequestBody requestBody;

        public Builder addHeader(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public Builder removeHeader(String key) {
            headers.remove(key);
            return this;
        }

        public Builder post(RequestBody requestBody) {
            method = Request.METHOD_POST;
            this.requestBody = requestBody;
            return this;
        }

        public Builder setHttpUrl(String url) {
            try {
                this.httpUrl = new HttpUrl(url);
                return this;
            } catch (MalformedURLException e) {
                throw new IllegalStateException("http url format error!", e);
            }
        }


        public Builder get() {
            method = Request.METHOD_GET;
            return this;
        }

        public Request build() {
            if (httpUrl == null) {
                throw new IllegalStateException("url is null!");
            }
            return new Request(this);
        }
    }
}
