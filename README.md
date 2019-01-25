# UpdateHttp

## 使用方法
```
    Request request = new Request.Builder()
                .setHttpUrl("http://baidu.com")
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
