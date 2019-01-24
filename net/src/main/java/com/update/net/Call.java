package com.update.net;

import com.update.net.http.BridgeInterceptor;
import com.update.net.cache.CacheInterceptor;
import com.update.net.http.CallServerInterceptor;
import com.update.net.connection.ConnectionInterceptor;
import com.update.net.http.InterceptorChain;
import com.update.net.http.RetryIntercepor;
import com.update.net.utils.L;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : liupu.
 * @date : 2019/1/23.
 */
public class Call {

    private HttpClient httpClient;
    private Request request;
    boolean executed;
    boolean canceled;

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public Request getRequest() {
        return request;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public Call(HttpClient httpClient, Request request) {
        this.httpClient = httpClient;
        this.request = request;
    }

    Response getResponseWithInterceptorChain() throws IOException {
        L.i("getResponseWithInterceptorChain");

        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.addAll(httpClient.getInterceptors());
        interceptors.add(new RetryIntercepor());
        interceptors.add(new BridgeInterceptor());
        interceptors.add(new CacheInterceptor());
        interceptors.add(new ConnectionInterceptor());
        interceptors.add(new CallServerInterceptor());

        InterceptorChain interceptorChain = new InterceptorChain(interceptors, 0, this, null);
        Response response = interceptorChain.proceed();
        return response;
    }

    public Call enqueue(CallBack callBack) {
        synchronized (this) {
            if (executed) {
                throw new IllegalStateException("This Call Already Executed!");
            }
            executed = true;
        }

        httpClient.getDispatcher().enqueue(new AsyncCall(callBack));
        return this;
    }

    final class AsyncCall implements Runnable {

        private CallBack callBack;

        public AsyncCall(CallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        public void run() {
            /**
             * 是否返回
             */
            boolean signalledCallback = false;
            try {
                Response response = getResponseWithInterceptorChain();
                if (canceled) {
                    signalledCallback = true;
                    callBack.onFailure(Call.this, new IOException("this task is canceled"));
                } else {
                    signalledCallback = true;
                    callBack.onResponse(Call.this, response);
                }
            } catch (IOException e) {
                if (!signalledCallback) {
                    callBack.onFailure(Call.this, e);
                }
            } finally {
                /**
                 *  将任务从调度器中移除
                 */
                httpClient.getDispatcher().finished(this);
            }

        }

        public String getHost() {
            return request.getHttpUrl().getHost();
        }
    }
}
