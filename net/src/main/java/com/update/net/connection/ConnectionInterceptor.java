package com.update.net.connection;

import com.update.net.HttpClient;
import com.update.net.HttpUrl;
import com.update.net.Interceptor;
import com.update.net.Request;
import com.update.net.Response;
import com.update.net.http.InterceptorChain;
import com.update.net.utils.L;

import java.io.IOException;

/**
 * @author : liupu.
 * @date : 2019/1/24.
 */
public class ConnectionInterceptor implements Interceptor {
    @Override
    public Response intercept(InterceptorChain interceptorChain) throws IOException {
        L.i("ConnectionInterceptor");

        Request request = interceptorChain.call().getRequest();
        HttpClient httpClient = interceptorChain.call().getHttpClient();
        HttpUrl httpUrl = request.getHttpUrl();
        HttpConnection httpConnection =
                httpClient.getConnectionPool().getHttpConnection(httpUrl.getHost(), httpUrl.getPort());

        if (null == httpConnection) {
            httpConnection = new HttpConnection();
        }

        httpConnection.setRequest(request);

        try {
            Response response = interceptorChain.proceed(httpConnection);
            if (response.isKeepAlive()) {
                httpClient.getConnectionPool().putHttpConnection(httpConnection);
            } else {
                httpConnection.close();
            }
            return response;
        } catch (IOException e) {
            httpConnection.close();
            throw e;
        }
    }
}
