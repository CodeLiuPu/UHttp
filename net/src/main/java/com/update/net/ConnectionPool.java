package com.update.net;

import android.support.annotation.NonNull;

import com.update.net.connection.HttpConnection;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author : liupu.
 * @date : 2019/1/24.
 */
public class ConnectionPool {
    private long keepAliveTime;
    private Deque<HttpConnection> httpConnections = new ArrayDeque<>();
    private boolean cleanUpRunning;

    private static final Executor excutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread thread = new Thread(r, "connection pool");
            thread.setDaemon(true);
            return thread;
        }
    });

    public ConnectionPool() {
        this(60L, TimeUnit.SECONDS);
    }

    public ConnectionPool(long keepAliveTime, TimeUnit timeUnit) {
        this.keepAliveTime = timeUnit.toMillis(keepAliveTime);
    }

    /**
     * 创建一个线程,用于定时检查,并清理无用的连接(无用 指 没使用的间期超过了保留时间)
     */
    private Runnable cleanUpRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                long now = System.currentTimeMillis();
                long waitDuration = cleanup(now); // 再过多久需要再次检测
                if (waitDuration == -1) {
                    return; //连接池为空，清理线程执行结束
                }

                if (waitDuration > 0) {
                    synchronized (ConnectionPool.this) {
                        try {
                            ConnectionPool.this.wait(waitDuration);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    };

    /**
     * 根据当前时间，清理无用的连接
     *
     * @param now
     */
    private long cleanup(long now) {

        /**
         * 最长闲置时间
         */
        long longestIdleDuration = -1;
        synchronized (this) {
            Iterator<HttpConnection> connectionIterator = httpConnections.iterator();
            while (connectionIterator.hasNext()) {
                HttpConnection httpConnection = connectionIterator.next();
                long idleDuration = now - httpConnection.lastUseTime();
                // 根据闲置时间来判断是否需要被清理
                if (idleDuration > keepAliveTime) {
                    connectionIterator.remove();
                    httpConnection.close();
                    continue;
                }
                if (idleDuration > longestIdleDuration) {
                    longestIdleDuration = idleDuration;
                }
            }
            if (longestIdleDuration >= 0) {
                return keepAliveTime - longestIdleDuration; //返回的值，下一次清理要多久以后
            }
            // 运行到这里 说明longestIdleDuration = -1，连接池中为空
            cleanUpRunning = false;
            return longestIdleDuration;
        }
    }

    public void putHttpConnection(HttpConnection httpConnection) {
        if (!cleanUpRunning) {
            cleanUpRunning = true;
            excutor.execute(cleanUpRunnable);
        }
        httpConnections.add(httpConnection);
    }

    /**
     * 根据服务器host和port 来获取可复用的连接
     *
     * @param host
     * @param port
     * @return
     */
    public synchronized HttpConnection getHttpConnection(String host, int port) {
        Iterator<HttpConnection> httpConnectionInteger = httpConnections.iterator();
        while (httpConnectionInteger.hasNext()){
            HttpConnection httpConnection = httpConnectionInteger.next();
            if (httpConnection.isSameAddress(host,port)){
                httpConnectionInteger.remove();
                return httpConnection;
            }
        }


        return null;
    }
}
