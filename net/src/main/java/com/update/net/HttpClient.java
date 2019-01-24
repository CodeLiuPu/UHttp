package com.update.net;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : liupu.
 * @date : 2019/1/23.
 */
public class HttpClient {

    private Dispatcher dispatcher;

    private List<Interceptor> interceptors;

    private int retryTimes;

    private ConnectionPool connectionPool;

    public int getRetryTimes() {
        return retryTimes;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public HttpClient(Builder builder) {
        this.dispatcher = builder.dispatcher;
        this.interceptors = builder.interceptors;
        this.connectionPool = builder.connectionPool;
        this.retryTimes = builder.retryTimes;
    }

    /**
     * 生成一个网络请求Call实例
     *
     * @param request
     * @return
     */
    public Call newCall(Request request) {
        return new Call(this, request);
    }

    public static final class Builder {

        private Dispatcher dispatcher = new Dispatcher();
        private List<Interceptor> interceptors = new ArrayList<>();
        private int retryTimes = 4;
        private ConnectionPool connectionPool = new ConnectionPool();

        public Builder addInterceptor(Interceptor interceptor) {
            interceptors.add(interceptor);
            return this;
        }

        public Builder setRetryTimes(int retryTimes) {
            this.retryTimes = retryTimes;
            return this;
        }

        public Builder setConnectionPool(ConnectionPool connectionPool) {
            this.connectionPool = connectionPool;
            return this;
        }

        public Builder setDispatcher(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
            return this;
        }

        public HttpClient build() {
            return new HttpClient(this);
        }
    }
}
