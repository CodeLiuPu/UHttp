# UpdateHttp

## 1 前言

    项目一直用的OkHttp,对其流程也一直是一知半解,最近复习了下网络知识和设计模式,又查阅了大佬的博客,把OkHttp的逻辑基本走通了,就仿着 OkHttp 写了一个框架,思路以及用法跟OkHttp一样,用于熟悉OkHttp的细节,加深下记忆

## 2 OkHttp请求流程

### 2.1 请求流程如下图
![okhttp_full_process.png](https://upload-images.jianshu.io/upload_images/61189-dd7fa8c172cf2591.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

    手写的UpdateHttp流程与OkHttp 一致 并精简了一部分,可以先看精简后的源码来熟悉OkHttp的源码

## 3 使用方式

### 3.1 get方式
``` java
Request request = new Request.Builder()
           .setHttpUrl("http://gank.io/api/today")
           .get()
           .build();

   HttpClient httpClient = new HttpClient.Builder()
           .setRetryTimes(3)
           .build();
   Call call = httpClient.newCall(request);
   call.enqueue(new CallBack() {
       @Override
       public void onFailure(Call call, Throwable throwable) {

       }

       @Override
       public void onResponse(Call call, Response response) {
           L.i("get body = " + response.getBody());
       }
   });
```

### 3.2 post方式
```
RequestBody requestBody = new RequestBody()
           .add("username", "hh123")
           .add("password", "123456");
   Request request = new Request.Builder()
           .setHttpUrl("http://www.wanandroid.com/user/login")
           .post(requestBody)
           .build();

   HttpClient httpClient = new HttpClient.Builder()
           .setRetryTimes(3)
           .build();
   Call call = httpClient.newCall(request);
   call.enqueue(new CallBack() {
       @Override
       public void onFailure(Call call, Throwable throwable) {

       }

       @Override
       public void onResponse(Call call, Response response) {
           L.i("post body = " + response.getBody());
       }
   });
```

## 4 具体实现(核心部分)

###包结构图

![QQ20190130-101123.png](https://upload-images.jianshu.io/upload_images/61189-9e2398ee68230426.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### Request

```
public class Request {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    private Map<String, String> headers;
    private String method;
    private HttpUrl httpUrl;
    private RequestBody requestBody;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public HttpUrl getHttpUrl() {
        return httpUrl;
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    public String getMethod() {
        return method;
    }

    public Request(Builder builder) {
        this.headers = builder.headers;
        this.method = builder.method;
        this.httpUrl = builder.httpUrl;
        this.requestBody = builder.requestBody;
    }

    public static final class Builder {
        private Map<String, String> headers = new HashMap<>();
        private String method = METHOD_GET;
        private HttpUrl httpUrl;
        private RequestBody requestBody;

        public Builder addHeader(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public Builder removeHeader(String key) {
            headers.remove(key);
            return this;
        }

        public Builder post(RequestBody requestBody) {
            method = Request.METHOD_POST;
            this.requestBody = requestBody;
            return this;
        }

        public Builder setHttpUrl(String url) {
            try {
                this.httpUrl = new HttpUrl(url);
                return this;
            } catch (MalformedURLException e) {
                throw new IllegalStateException("http url format error!", e);
            }
        }


        public Builder get() {
            method = Request.METHOD_GET;
            return this;
        }

        public Request build() {
            if (httpUrl == null) {
                throw new IllegalStateException("url is null!");
            }
            return new Request(this);
        }
    }
}
```

    使用了建造者模式

### HttpClient

```
public class HttpClient {

    private Dispatcher dispatcher;

    private List<Interceptor> interceptors;

    private int retryTimes;

    private ConnectionPool connectionPool;

    public int getRetryTimes() {
        return retryTimes;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public HttpClient(Builder builder) {
        this.dispatcher = builder.dispatcher;
        this.interceptors = builder.interceptors;
        this.connectionPool = builder.connectionPool;
        this.retryTimes = builder.retryTimes;
    }

    /**
     * 生成一个网络请求Call实例
     *
     * @param request
     * @return
     */
    public Call newCall(Request request) {
        return new Call(this, request);
    }

    public static final class Builder {

        private Dispatcher dispatcher = new Dispatcher();
        private List<Interceptor> interceptors = new ArrayList<>();
        private int retryTimes = 4;
        private ConnectionPool connectionPool = new ConnectionPool();

        public Builder addInterceptor(Interceptor interceptor) {
            interceptors.add(interceptor);
            return this;
        }

        public Builder setRetryTimes(int retryTimes) {
            this.retryTimes = retryTimes;
            return this;
        }

        public Builder setConnectionPool(ConnectionPool connectionPool) {
            this.connectionPool = connectionPool;
            return this;
        }

        public Builder setDispatcher(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
            return this;
        }

        public HttpClient build() {
            return new HttpClient(this);
        }
    }
}
```

    使用了建造者模式
    内部维护了一个 Interceptor 列表,用于添加自定义的 Interceptor
    维护了一个 Dispatcher 用于管理 请求


### Call

```
public class Call {

    private HttpClient httpClient;
    private Request request;
    boolean executed;
    boolean canceled;

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public Request getRequest() {
        return request;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public Call(HttpClient httpClient, Request request) {
        this.httpClient = httpClient;
        this.request = request;
    }

    Response getResponseWithInterceptorChain() throws IOException {
        L.i("getResponseWithInterceptorChain");

        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.addAll(httpClient.getInterceptors());
        interceptors.add(new RetryIntercepor());
        interceptors.add(new BridgeInterceptor());
        interceptors.add(new CacheInterceptor());
        interceptors.add(new ConnectionInterceptor());
        interceptors.add(new CallServerInterceptor());

        InterceptorChain interceptorChain = new InterceptorChain(interceptors, 0, this, null);
        Response response = interceptorChain.proceed();
        return response;
    }

    public Call enqueue(CallBack callBack) {
        synchronized (this) {
            if (executed) {
                throw new IllegalStateException("This Call Already Executed!");
            }
            executed = true;
        }

        httpClient.getDispatcher().enqueue(new AsyncCall(callBack));
        return this;
    }

    final class AsyncCall implements Runnable {

        private CallBack callBack;

        public AsyncCall(CallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        public void run() {
            /**
             * 是否返回
             */
            boolean signalledCallback = false;
            try {
                Response response = getResponseWithInterceptorChain();
                if (canceled) {
                    signalledCallback = true;
                    callBack.onFailure(Call.this, new IOException("this task is canceled"));
                } else {
                    signalledCallback = true;
                    callBack.onResponse(Call.this, response);
                }
            } catch (IOException e) {
                if (!signalledCallback) {
                    callBack.onFailure(Call.this, e);
                }
            } finally {
                /**
                 *  将任务从调度器中移除
                 */
                httpClient.getDispatcher().finished(this);
            }

        }

        public String getHost() {
            return request.getHttpUrl().getHost();
        }
    }
}
```

    在Call 内部 来添加拦截器链
    在拦截器链中进行了真正的网络请求部分


## 5 总结:

    引用别人的话 "我们不重复造轮子不表示我们不需要知道轮子该怎么造及如何更好的造"
    不去重复造轮子,但我感觉还是去亲手实现下,才会对其中的细节部分了解的更加的清晰,通过仿造OkHttp,可以学到其中的思想,以及精美的设计模式用法,这样就够了

[简书地址](https://www.jianshu.com/p/857f113a2f8a)
