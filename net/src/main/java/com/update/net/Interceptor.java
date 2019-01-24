package com.update.net;

import com.update.net.http.InterceptorChain;

import java.io.IOException;

/**
 * @author : liupu.
 * @date : 2019/1/24.
 */
public interface Interceptor {

    Response intercept(InterceptorChain interceptorChain) throws IOException;
}
