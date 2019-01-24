package com.update.net;

import android.support.annotation.NonNull;

import com.update.net.utils.L;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author : liupu.
 * @date : 2019/1/23.
 */
public class Dispatcher {
    /**
     * 最多同时支持请求的数量
     */
    private int maxRequests;

    /**
     * 同一个host最多请求的数量
     */
    private int maxRequestPerHost;

    private ExecutorService executorService;
    /**
     * 等待队列
     */
    private final Deque<Call.AsyncCall> readyAsyncCalls = new ArrayDeque<>();
    /**
     * 请求中队列
     */
    private final Deque<Call.AsyncCall> runningAsyncCalls = new ArrayDeque<>();


    public Dispatcher() {
        this(64, 5);
    }

    public Dispatcher(int maxRequests, int maxRequestPerHost) {
        this.maxRequests = maxRequests;
        this.maxRequestPerHost = maxRequestPerHost;
    }

    /**
     * 初始化 线程池
     *
     * @return
     */
    public synchronized ExecutorService initExecutorService() {
        if (null == executorService) {
            ThreadFactory threadFactory = new ThreadFactory() {
                @Override
                public Thread newThread(@NonNull Runnable runnable) {
                    Thread thread = new Thread(runnable, "Http Client Thread");
                    return thread;
                }
            };

            /**
             * 按照 OkHttp 线程池创建 单个线程在闲置的时候保留60s
             */
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                    60L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), threadFactory);
        }
        return executorService;
    }

    /**
     * 将请求加入请求队列
     *
     * @param asyncCall
     */
    public void enqueue(Call.AsyncCall asyncCall) {
        /**
         * 先判断正在运行的队列是否已满 & 同一个Host请求的数量是否超过规定数量
         */
        if (runningAsyncCalls.size() < maxRequests
                && getRunningPreHostCount(asyncCall) < maxRequestPerHost) {
            runningAsyncCalls.add(asyncCall);
            initExecutorService().execute(asyncCall);
        } else {
            /**
             * 不满足条件 添加至等待队列
             */
            readyAsyncCalls.add(asyncCall);
        }
    }

    private int getRunningPreHostCount(Call.AsyncCall asyncCall) {
        int count = 0;
        for (Call.AsyncCall runningAsynvall : runningAsyncCalls) {
            if (runningAsynvall.getHost().equals(asyncCall.getHost())) {
                count++;
            }
        }
        return count;
    }

    public void finished(Call.AsyncCall asyncCall) {
        synchronized (this) {
            runningAsyncCalls.remove(asyncCall);
            checkReadyCalls();
        }
    }

    /**
     * 检查等待队列中的请求
     */
    private void checkReadyCalls() {
        /**
         * 达到同时最大请求数 || 没有等待执行的任务
         * 直接返回
         */
        L.i("runningAsyncCalls size " + runningAsyncCalls.size());
        if (runningAsyncCalls.size() >= maxRequests
                || readyAsyncCalls.isEmpty()) {
            return;
        }
        Iterator<Call.AsyncCall> asyncCallIterator = readyAsyncCalls.iterator();
        while (asyncCallIterator.hasNext()) {
            Call.AsyncCall asyncCall = asyncCallIterator.next();
            /**
             * 如果添加任务后 小于 单个Host下同时允许的最大数就可以执行
             */
            if (getRunningPreHostCount(asyncCall) < maxRequestPerHost) {
                asyncCallIterator.remove();
                runningAsyncCalls.add(asyncCall);
                executorService.execute(asyncCall);
            }

            if (runningAsyncCalls.size() >= maxRequests) {
                return;
            }

        }
    }
}
