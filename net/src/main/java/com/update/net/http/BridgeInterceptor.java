package com.update.net.http;

import com.update.net.Interceptor;
import com.update.net.Request;
import com.update.net.RequestBody;
import com.update.net.Response;
import com.update.net.utils.L;

import java.io.IOException;
import java.util.Map;

/**
 * @author : liupu.
 * @date : 2019/1/24.
 */
public class BridgeInterceptor implements Interceptor {
    @Override
    public Response intercept(InterceptorChain interceptorChain) throws IOException {
        L.i("BridgeInterceptor");

        Request request = interceptorChain.call().getRequest();
        Map<String, String> headers = request.getHeaders();
        // 补全 host
        if (!headers.containsKey(HttpCodec.HEAD_HOST)) {
            headers.put(HttpCodec.HEAD_HOST, request.getHttpUrl().getHost());
        }

        if (!headers.containsKey(HttpCodec.HEAD_CONNECTION)) {
            headers.put(HttpCodec.HEAD_CONNECTION, HttpCodec.HEAD_VALUE_KEEP_ALIVE);
        }

        if (null != request.getRequestBody()) {
            String contentType = RequestBody.getContentType();
            if (null != contentType) {
                headers.put(HttpCodec.HEAD_CONTENT_TYPE, contentType);
            }

            long contentLength = request.getRequestBody().getContentLength();

            if (-1 != contentLength) {
                headers.put(HttpCodec.HEAD_CONTENT_LENGTH, Long.toString(contentLength));
            }
        }
        return interceptorChain.proceed();
    }
}
