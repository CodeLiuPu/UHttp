package com.update.net.connection;

import android.text.TextUtils;

import com.update.net.HttpUrl;
import com.update.net.Request;
import com.update.net.http.HttpCodec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

/**
 * @author : liupu.
 * @date : 2019/1/24.
 */
public class HttpConnection {
    Socket socket;
    long lastUseTime;

    private Request request;
    private InputStream inputStream;
    private OutputStream outputStream;

    public void setRequest(Request request) {
        this.request = request;
    }

    public void updateLastUseTime() {
        lastUseTime = System.currentTimeMillis();
    }

    public boolean isSameAddress(String host, int port) {
        if (null == socket) {
            return false;
        }

        return TextUtils.equals(request.getHttpUrl().getHost(), host) && request.getHttpUrl().getPort() == port;
    }

    /**
     * 创建 Socket 链接
     *
     * @throws IOException
     */
    private void createSocket() throws IOException {

        if (null == socket || socket.isClosed()) {
            HttpUrl httpUrl = request.getHttpUrl();
            if (httpUrl.getProtocol().equalsIgnoreCase(HttpCodec.PROTOCOL_HTTPS)) {
                socket = SSLSocketFactory.getDefault().createSocket();
            } else {
                socket = new Socket();
            }
            socket.connect(new InetSocketAddress(httpUrl.getHost(), httpUrl.getPort()));
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        }

    }

    /**
     * 关闭 Socket 链接
     */
    public void close() {
        if (null != socket) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public InputStream call(HttpCodec httpCodec) throws IOException {
        createSocket();

        httpCodec.writeRequest(outputStream, request);
        return inputStream;
    }

    public long lastUseTime(){
        return lastUseTime;
    }
}
