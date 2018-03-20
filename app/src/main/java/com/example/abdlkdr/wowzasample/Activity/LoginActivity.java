package com.example.abdlkdr.wowzasample.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.abdlkdr.wowzasample.R;
import com.example.abdlkdr.wowzasample.Util.Constant;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by abdlkdr on 5.03.2018.
 */

public class LoginActivity  extends AppCompatActivity{

    private Button btnLogin;

    private EditText editTxtUsername;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        bindView();
        setViewAction();
    }

    private void bindView() {
        btnLogin = (Button) findViewById(R.id.btnLogin);
        editTxtUsername = (EditText) findViewById(R.id.editTextUsername);
    }

    private void setViewAction() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTxtUsername.getText().toString();
                Log.e(TAG, username);
                checkLoginResult(editTxtUsername.getText().toString());
            }
        });
    }

    private void checkLoginResult(final String username) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(Constant.SYSTEMIP)
                .port(8080)
                .addPathSegment("loginUser")
                .addQueryParameter("username", username)
                .build();
        String myUrl = url.toString();
        Log.e(TAG, myUrl);
        Request request = new Request.Builder()
                .url(myUrl)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
                Log.e("LoginActivity", "Failure Line 65");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String tempStatus = response.body().string();
                if (tempStatus.contentEquals("ok")) {
                    Intent intent = new Intent(LoginActivity.this, ListUserActivity.class);
                    intent.putExtra("username",username);
                    setUserStatus(username,Constant.ONLINE);
                    startActivity(intent);
                    Log.e("LoginActivity", " :   Succes");
                } else {
                    Intent ıntent = new Intent(LoginActivity.this, LoginActivity.class);
                    Log.e("LoginActivity", " :   Failure");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "Kullanıcı adınız yanlış", Toast.LENGTH_SHORT).show();
                        }
                    });
                    startActivity(ıntent);
                }
            }
        });
    }
    //Change the status if status is online then change status to offline
    //İf status is offline change status to online
    private void setUserStatus(String username,String status) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(Constant.SYSTEMIP)
                .port(8080)
                .addPathSegment("setUserStatus")
                .addQueryParameter("username", username)
                .addQueryParameter("status",status)
                .build();
        String myUrl = url.toString();
        Request request = new Request.Builder()
                .url(myUrl)
                .build();
        client.newCall(request).enqueue(new Callback() {
            String status = "";

            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("setUserStatus", "Fail line");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                status = response.body().string();
            }
        });
    }


}
