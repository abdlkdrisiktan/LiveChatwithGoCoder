package com.example.abdlkdr.wowzasample.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.abdlkdr.wowzasample.Adapter.RecyclerViewUserAdapter;
import com.example.abdlkdr.wowzasample.Model.RequestLiveChat;
import com.example.abdlkdr.wowzasample.Model.User;
import com.example.abdlkdr.wowzasample.R;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by abdlkdr on 6.03.2018.
 */

public class ListUserActivity extends AppCompatActivity {

    public RecyclerView recyclerView;

    private Button btnRandomMatch;

    private RecyclerViewUserAdapter recyclerViewUserAdapter;

    public ArrayList<User> userArrayList = new ArrayList<>();

    private static final String getAllUserUrl = "http://10.106.148.12:8080/getAllUser";

    private static final String TAG = "ListUserActivity";

    public static String lastUsername = "";

    public static final String MY_PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userlist);
        lastUsername = getIntentExtra(savedInstanceState);
        bindView();
        getAllUser();
        getIntentExtra(savedInstanceState);
        checkRequestIsExist();
        setViewAction();
    }

    private void bindView() {
        btnRandomMatch = (Button) findViewById(R.id.btnRandomMatch);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    }

    private void getAllUser() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(getAllUserUrl)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "Failure");
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final String myResponse = response.body().string();
                final String username = lastUsername;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.body() != null) {
                            try {
                                JSONArray jsonArray = new JSONArray(myResponse);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    User user = new User();
                                    boolean isUsernameExist = false;
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    if (!jsonObject.isNull("status")) {
                                        Log.e(TAG, "status line");
                                        if (jsonObject.getString("status").contentEquals("online")) {
                                            user.setStatus(jsonObject.getString("status"));
                                            if (!jsonObject.isNull("id")) {
                                                user.setId(jsonObject.getString("id"));
                                            }
                                            if (!jsonObject.isNull("username")) {
                                                user.setUsername(jsonObject.getString("username"));
                                                if (user.getUsername().contentEquals(username)) {
                                                    Log.e(TAG, "username :   " + username + "user model username :   " + user.getUsername());
                                                    isUsernameExist = true;
                                                    if (isUsernameExist == true) {
                                                        userArrayList.remove(user);
                                                    }
                                                }
                                                Log.e(TAG, "bbbbbbbbb" + user.getUsername());
                                            }
                                        }
                                    }
                                    if ((user.getUsername() != null) && isUsernameExist == false)
                                        userArrayList.add(user);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        recyclerViewUserAdapter = new RecyclerViewUserAdapter(getApplicationContext(), userArrayList);
                        recyclerView.setAdapter(recyclerViewUserAdapter);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ListUserActivity.this);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setHasFixedSize(true);
//                        recyclerViewUserAdapter.notifyDataSetChanged();
//                                                new Timer().schedule(new TimerTask() {
//                            @Override
//                            public void run() {
////                                Log.e(TAG,"DENEME");
//                                getAllUser();
//                            }
//                        },1000);
                    }
                });

            }
        });

    }

    private String getIntentExtra(Bundle savedInstanceState) {
        String username = "";
        if (savedInstanceState == null) {
            User user;
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                user = new User();
                return username;
            } else {
                username = extras.getString("username");
                final String finalUsername = username;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ListUserActivity.this, "Username  :   " + finalUsername, Toast.LENGTH_SHORT).show();

                    }
                });
                return finalUsername;
            }
        }
        return username;
    }

    private void checkRequestIsExist() {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("10.106.148.12")
                .port(8080)
                .addPathSegment("checkRequestIsExist")
                .addQueryParameter("toUser", lastUsername)
                .build();
        Request request = new Request.Builder().url(url.toString()).build();
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {

                    }

                    @Override
                    public void onResponse(final Response response) throws IOException {
                        final String myFinalResponse = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (response.body() != null) {
                                    String result = myFinalResponse;
                                    if (result.contentEquals("yes")) {
                                        alertDialogBuilder();
                                    } else {
                                        new Timer().schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                checkRequestIsExist();
                                            }
                                        }, 1000);
                                    }
                                }

                            }
                        });
                    }
                });
    }

    private void alertDialogBuilder() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        builder.setTitle("Eşleşme Bulundu")
                .setMessage("Birisiyle eşleşdin konuşmaya başlamak ister misin?")
                .setPositiveButton(R.string.alert_dialog_btn_yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        Intent ıntent = new Intent(ListUserActivity.this, MainActivity.class);
                        Log.e(TAG,"yes button is active");

                        changeAcceptedStatus("true");
                        final String otherUsername = getRequestDataInformation();
                        Log.d(TAG, "onClick: other username is  :   "+otherUsername);
                        ıntent.putExtra("otherUsername",otherUsername);
                        startActivity(ıntent);
//                        ıntent.putExtra("toUsername",)
//burada main activity gidecek ve konuşamnın başlamasını bekleycek tabı burada request gelen karsının username ihtiyacımız var cunku wowza da
                        //url oluştururken 1. kullanıcı/ 2. kullanıcı sekılde yapacağız
                    }
                })
                .setNegativeButton(R.string.alert_dialog_btn_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG,"no button is active");
                        changeAcceptedStatus("false");

//                        checkRequestIsExist();
//                        burada public void changeRequestLiveChatStatus(String user, String toUser)
                        //method cağırıp daha sonrasında  kontrol edecek ve metot içerisnde accepted secenıngı false etmek gerekir bir daha
                        //istek göndermemesi için ardı ardına
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();


    }

    private void changeAcceptedStatus(String acceptedStatus) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("10.106.148.12")
                .port(8080)
                .addPathSegment("changeAcceptedStatus")
                .addQueryParameter("toUser", lastUsername)
                .addQueryParameter("acceptedStatus",acceptedStatus)
                .build();
        Request request = new Request.Builder().url(url.toString()).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {

            }
        });
    }

    private void setViewAction() {
        btnRandomMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRequestLiveChat();
                getAcceptedStatus();
            }
        });
    }

    private void getAcceptedStatus(){
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("10.106.148.12")
                .port(8080)
                .addPathSegment("getAcceptedStatus")
                .addQueryParameter("user", lastUsername)
                .build();
        final Request  request = new Request.Builder().url(url.toString()).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final String myResponse = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.body()!=null){
                            String result = myResponse;
                            if (result.contentEquals("true" )){
                                Intent ıntent = new Intent(ListUserActivity.this,MainActivity.class);
                                startActivity(ıntent);
                            }else {
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        getAcceptedStatus();
                                    }
                                },1000);
                            }
                        }
                    }
                });
            }
        });
    }

    private void createRequestLiveChat() {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .port(8080)
                .host("10.106.148.12")
                .addPathSegment("createRequestLiveChat")
                .addQueryParameter("user", lastUsername)
                .build();
        final Request request = new Request.Builder()
                .url(url.toString())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final String myResponse = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.body() != null) {
                            JSONObject jsonObject;
                            RequestLiveChat requestLiveChat = new RequestLiveChat();
                            try {
                                jsonObject = new JSONObject(myResponse);
                                if (!jsonObject.isNull("id")) {
                                    requestLiveChat.setId(jsonObject.getString("id"));
                                }

                                if (!jsonObject.isNull("status")) {
                                    requestLiveChat.setStatus(jsonObject.getString("status"));
                                }
                                if (!jsonObject.isNull("liveChatUrl")) {
                                    requestLiveChat.setLiveChatUrl(jsonObject.getString("liveChatUrl"));
                                }
                                if (!jsonObject.isNull("accepted")) {
                                    requestLiveChat.setAccepted(jsonObject.getBoolean("accepted"));
                                }
                                if (!jsonObject.isNull("user")) {
                                    JSONArray array = jsonObject.getJSONArray("user");
                                    User user = new User();
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject object = array.getJSONObject(i);
                                        if (!object.isNull("id")) {
                                            user.setId(object.getString("id"));
                                        }
                                        if (!object.isNull("username")) {
                                            user.setUsername(object.getString("username"));
                                        }
                                        if (!object.isNull("status")) {
                                            user.setStatus(object.getString("status"));
                                        }
                                    }
                                    requestLiveChat.setUser(user);
                                }
                                if (!jsonObject.isNull("toUser")) {
                                    JSONArray array = jsonObject.getJSONArray("toUser");
                                    User toUser = new User();
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject object = array.getJSONObject(i);
                                        if (!object.isNull("id")) {
                                            toUser.setId(object.getString("id"));
                                        }
                                        if (!object.isNull("username")) {
                                            toUser.setUsername(object.getString("username"));
                                        }
                                        if (!object.isNull("status")) {
                                            toUser.setStatus(object.getString("status"));
                                        }
                                    }
                                    requestLiveChat.setToUser(toUser);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }

    private String  getRequestDataInformation() {

        final RequestLiveChat requestLiveChat = new RequestLiveChat();
        final SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .port(8080)
                .host("10.106.148.12")
                .addPathSegment("getRequest")
                .addQueryParameter("user", lastUsername)
                .build();
        Request request = new Request.Builder().url(url.toString()).build();
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {

                    }

                    @Override
                    public void onResponse(final Response response) throws IOException {
                        final String myResponse = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (response.body() != null) {
                                    JSONObject jsonObject;

                                    try {
                                        jsonObject = new JSONObject(myResponse);
                                        if (!jsonObject.isNull("id")) {
                                            requestLiveChat.setId(jsonObject.getString("id"));
                                        }

                                        if (!jsonObject.isNull("status")) {
                                            requestLiveChat.setStatus(jsonObject.getString("status"));
                                        }
                                        if (!jsonObject.isNull("liveChatUrl")) {
                                            requestLiveChat.setLiveChatUrl(jsonObject.getString("liveChatUrl"));
                                        }
                                        if (!jsonObject.isNull("accepted")) {
                                            requestLiveChat.setAccepted(jsonObject.getBoolean("accepted"));
                                        }
                                        if (!jsonObject.isNull("user")) {
                                            JSONArray array = jsonObject.getJSONArray("user");
                                            User user = new User();
                                            for (int i = 0; i < array.length(); i++) {
                                                JSONObject object = array.getJSONObject(i);
                                                if (!object.isNull("id")) {
                                                    user.setId(object.getString("id"));
                                                }
                                                if (!object.isNull("username")) {

                                                    user.setUsername(object.getString("username"));
                                                    editor.putString("username",user.getUsername());
                                                }
                                                if (!object.isNull("status")) {
                                                    user.setStatus(object.getString("status"));
                                                }
                                            }
                                            requestLiveChat.setUser(user);
                                        }
                                        if (!jsonObject.isNull("toUser")) {
                                            JSONArray array = jsonObject.getJSONArray("toUser");
                                            User toUser = new User();
                                            for (int i = 0; i < array.length(); i++) {
                                                JSONObject object = array.getJSONObject(i);
                                                if (!object.isNull("id")) {
                                                    toUser.setId(object.getString("id"));
                                                }
                                                if (!object.isNull("username")) {
                                                    toUser.setUsername(object.getString("username"));
                                                    editor.putString("toUser",toUser.getUsername());
                                                }
                                                if (!object.isNull("status")) {
                                                    toUser.setStatus(object.getString("status"));
                                                }
                                            }
                                            requestLiveChat.setToUser(toUser);

                                        }
                                        editor.apply();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                    }
                });
        return requestLiveChat.getUser().getUsername();

    }

}
