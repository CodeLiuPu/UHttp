package com.update.updatehttp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.update.net.Call;
import com.update.net.CallBack;
import com.update.net.HttpClient;
import com.update.net.Request;
import com.update.net.RequestBody;
import com.update.net.Response;
import com.update.net.utils.L;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_get).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doGet();
            }
        });

        findViewById(R.id.btn_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPost();
            }
        });
    }

    private  void doGet(){
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
    }


    private  void doPost(){

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
    }
}
