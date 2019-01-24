package com.update.net.utils;


import android.util.Log;

/**
 * @author : liupu.
 * @date : 2019/1/24.
 * 日志控制类
 */
public class L {
    private static String TAG = "UPDATE HTTP";

    public static void i(String message) {
        Log.i(TAG, TAG + " " + message);
        /**
         * 单元测试无法使用Log 所以使用 System.out
         */
//        System.out.println(TAG + " message = " + message);
    }
}
