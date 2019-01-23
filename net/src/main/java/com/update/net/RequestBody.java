package com.update.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : liupu.
 * @date : 2019/1/22.
 */
public class RequestBody {

    /**
     * 表单提交 使用urlencoded编码,这里也可以使用json方式
     */
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

    Map<String, String> encodebodys = new HashMap<>();

    public static String getContentType() {
        return CONTENT_TYPE;
    }

    public int getContentLength(){
        return getBody().getBytes().length;
    }
    public String getBody() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : encodebodys.entrySet()) {
            sb.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append("&");
        }
        if (sb.length() != 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    // 通过jdk的url编码
    public RequestBody add(String key, String value) {
        try {
            encodebodys.put(URLEncoder.encode(key, HttpCodec.
                    ENCODE), URLEncoder.encode(value, HttpCodec.ENCODE));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;
    }
}