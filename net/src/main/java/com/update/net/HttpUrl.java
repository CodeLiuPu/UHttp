package com.update.net;

import com.update.net.utils.TextUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author : liupu.
 * @date : 2019/1/23.
 * 存储http的url请求信息 如host,port,protocol,file
 */
public class HttpUrl {
    private String protocol;
    private String host;
    private String file;
    private int port;

    public HttpUrl(String url) throws MalformedURLException {
        URL localUrl = new URL(url);
        protocol = localUrl.getProtocol();
        host = localUrl.getHost();
        file = TextUtils.isEmpty(localUrl.getFile()) ? "/" : localUrl.getFile();
        port = localUrl.getPort() == -1 ? localUrl.getDefaultPort() : localUrl.getPort();
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public String getFile() {
        return file;
    }

    public int getPort() {
        return port;
    }
}
