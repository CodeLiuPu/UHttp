package com.update.net.http;

import com.update.net.Call;
import com.update.net.Interceptor;
import com.update.net.Response;
import com.update.net.http.InterceptorChain;
import com.update.net.utils.L;

import java.io.IOException;

/**
 * @author : liupu.
 * @date : 2019/1/24.
 */
public class RetryIntercepor implements Interceptor {
    @Override
    public Response intercept(InterceptorChain interceptorChain) throws IOException {
        L.i("RetryIntercepor");

        Call call = interceptorChain.call();
        IOException ioException = null;

        for (int i = 0; i < call.getHttpClient().getRetryTimes(); i++) {
            if (call.isCanceled()) {
                throw new IOException("this task is canceled");
            }
            try {
                Response response = interceptorChain.proceed();
                return response;
            } catch (IOException e) {
                ioException = e;
            }
        }
        throw ioException;
    }
}
