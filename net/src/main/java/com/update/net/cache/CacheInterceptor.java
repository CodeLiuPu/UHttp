package com.update.net.cache;

import com.update.net.Interceptor;
import com.update.net.Response;
import com.update.net.http.InterceptorChain;
import com.update.net.utils.L;

import java.io.IOException;

/**
 * @author : liupu.
 * @date : 2019/1/24.
 */
public class CacheInterceptor implements Interceptor {
    @Override
    public Response intercept(InterceptorChain interceptorChain) throws IOException {
        L.i("CacheInterceptor");


        return interceptorChain.proceed();
    }
}
