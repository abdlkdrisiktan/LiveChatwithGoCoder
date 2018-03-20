package com.example.abdlkdr.wowzasample.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.abdlkdr.wowzasample.Adapter.RecyclerViewUserAdapter;
import com.example.abdlkdr.wowzasample.Model.ChatUsers;
import com.example.abdlkdr.wowzasample.Model.RequestLiveChat;
import com.example.abdlkdr.wowzasample.Model.User;
import com.example.abdlkdr.wowzasample.R;
import com.example.abdlkdr.wowzasample.Util.Constant;
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

    private static final String TAG = "ListUserActivity";

    public static String lastUsername = "";

    public SharedPreferences sharedpreferences;

    public ChatUsers chatUsers = new ChatUsers();

    public Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userlist);
        sharedpreferences = getSharedPreferences(Constant.MyPREFERENCES, Context.MODE_PRIVATE);
        lastUsername = getIntentExtra(savedInstanceState);
        bindView();
        getAllUser(lastUsername);
        getIntentExtra(savedInstanceState);
        checkRequestIsExist();
        setViewAction();

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setMessage(R.string.alert_dialog_exit_text)
                .setPositiveButton(R.string.alert_dialog_btn_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setUserStatus(Constant.OFFLINE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.clear();
                        editor.apply();
                        finish();

                    }
                })
                .setNegativeButton(R.string.alert_dialog_btn_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    private void bindView() {
        btnRandomMatch = (Button) findViewById(R.id.btnRandomMatch);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    }

    //The list display online users
    private void getAllUser(final String username) {
        OkHttpClient client = new OkHttpClient();
//        private static final String getAllUserUrl = "http://10.106.148.11:8080/getAllUser";
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(Constant.SYSTEMIP)
                .port(8080)
                .addPathSegment("getAllUser")
                .addQueryParameter("username", username)
                .build();
        Request request = new Request.Builder()
                .url(url.toString())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "Failure");
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final String myResponse = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.body() != null) {
                            try {
                                JSONArray jsonArray = new JSONArray(myResponse);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    User user = new User();
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    if (!jsonObject.isNull("status")) {
                                        if (jsonObject.getString("status").contentEquals("online")) {
                                            user.setStatus(jsonObject.getString("status"));
                                            if (!jsonObject.isNull("id")) {
                                                user.setId(jsonObject.getString("id"));
                                            }
                                            if (!jsonObject.isNull("username")) {
                                                user.setUsername(jsonObject.getString("username"));
                                            }
                                            userArrayList.add(user);
                                        } else {
                                            userArrayList.remove(user);
                                        }

                                    }
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
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                userArrayList.clear();
                                getAllUser(username);
                            }
                        }, 1000);
                    }
                });

            }
        });

    }

    //Getting username
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

    //Check any request comes
    private void checkRequestIsExist() {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(Constant.SYSTEMIP)
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
                                        timer.cancel();
                                        timer.purge();
                                    } else {
                                        timer.schedule(new TimerTask() {
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
                .setMessage("Birisiyle eşleştin konuşmaya başlamak ister misin?")
                .setPositiveButton(R.string.alert_dialog_btn_yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        changeAcceptedStatus("true");
                        getRequestData();
                    }
                })
                .setNegativeButton(R.string.alert_dialog_btn_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        changeAcceptedStatus("false");
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    //İf accept the live chat make isAccepted trueq
    private void changeAcceptedStatus(String acceptedStatus) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(Constant.SYSTEMIP)
                .port(8080)
                .addPathSegment("changeAcceptedStatus")
                .addQueryParameter("toUser", lastUsername)
                .addQueryParameter("acceptedStatus", acceptedStatus)
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

    //Button click
    private void setViewAction() {
        btnRandomMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRequestLiveChat();
                getAcceptedStatus();
            }
        });
    }

    //Controlling if it's accepted
    private void getAcceptedStatus() {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(Constant.SYSTEMIP)
                .port(8080)
                .addPathSegment("getAcceptedStatus")
                .addQueryParameter("user", lastUsername)
                .build();
        final Request request = new Request.Builder().url(url.toString()).build();
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
                            String result = myResponse;
                            if (result.contentEquals("true")) {
                                getRequestData();
                            } else {
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        getAcceptedStatus();
                                    }
                                }, 1000);
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
                .host(Constant.SYSTEMIP)
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
                                    JSONObject array = jsonObject.getJSONObject("user");
                                    User user = new User();
                                    if (!array.isNull("id")) {
                                        user.setId(array.getString("id"));
                                    }
                                    if (!array.isNull("username")) {
                                        user.setUsername(array.getString("username"));
                                    }
                                    if (!array.isNull("status")) {
                                        user.setStatus(array.getString("status"));
                                    }
                                    requestLiveChat.setUser(user);
                                }
                                if (!jsonObject.isNull("toUser")) {
                                    JSONObject array = jsonObject.getJSONObject("toUser");
                                    User toUser = new User();
                                    if (!array.isNull("id")) {
                                        toUser.setId(array.getString("id"));
                                    }
                                    if (!array.isNull("username")) {
                                        toUser.setUsername(array.getString("username"));
                                    }
                                    if (!array.isNull("status")) {
                                        toUser.setStatus(array.getString("status"));
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

    //Change the status if status is online then change status to offline
    //İf status is offline change status to online
    private void setUserStatus(String status) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(Constant.SYSTEMIP)
                .port(8080)
                .addPathSegment("setUserStatus")
                .addQueryParameter("username", ListUserActivity.lastUsername)
                .addQueryParameter("status", status)
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
                Log.e(TAG, "onResponse: DENEME ");
            }
        });
    }

    private void getRequestData() {
        final RequestLiveChat requestLiveChat = new RequestLiveChat();
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .port(8080)
                .host(Constant.SYSTEMIP)
                .addPathSegment("getRequestData")
                .addQueryParameter("username", lastUsername)
                .build();
        Log.e(TAG, "getRequestDataInformation: " + url.toString());
        Request request = new Request.Builder().url(url.toString()).build();
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
                            Log.e(TAG, "run: if içerisi");
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
                                    JSONObject array = jsonObject.getJSONObject("user");
                                    User user = new User();
                                    if (!array.isNull("id")) {
                                        user.setId(array.getString("id"));
                                    }
                                    if (!array.isNull("username")) {
                                        user.setUsername(array.getString("username"));
                                        chatUsers.setUserUsername(user.getUsername());
                                        Log.e(TAG, "run: Line 504 User username is  :" + user.getUsername());
                                    }
                                    if (!array.isNull("status")) {
                                        user.setStatus(array.getString("status"));
                                    }
                                    requestLiveChat.setUser(user);
                                }
                                if (!jsonObject.isNull("toUser")) {
                                    JSONObject array = jsonObject.getJSONObject("toUser");
                                    User toUser = new User();
                                    if (!array.isNull("id")) {
                                        toUser.setId(array.getString("id"));
                                    }
                                    if (!array.isNull("username")) {
                                        toUser.setUsername(array.getString("username"));
                                        Log.e(TAG, "run: Line 519" + "    toUser username is : " + toUser.getUsername());
                                        chatUsers.setToUserUsername(toUser.getUsername());
                                    }
                                    if (!array.isNull("status")) {
                                        toUser.setStatus(array.getString("status"));
                                    }
                                    requestLiveChat.setToUser(toUser);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //editor.commit() writes the data synchronously (blocking the thread its called from). It then informs you about the success of the operation.
                            //apply() schedules the data to be written asynchronously. It does not inform you about the success of the operation.
                            if (!lastUsername.contentEquals(chatUsers.getUserUsername())){
                                Intent ıntent = new Intent(ListUserActivity.this, MainActivity.class);
                                ıntent.putExtra(Constant.OTHERUSERNAME, chatUsers.getUserUsername());
                                Log.e(TAG, "İSTEĞİN GELDİĞİ TARAF");
                                Log.e(TAG, "İSTEĞİ GÖNDEREN KİŞİ    :   "+chatUsers.getUserUsername()+" İSTEĞİ ALAN TARAF   :   "+lastUsername );
                                timer.cancel();
                                timer.purge();
                                startActivity(ıntent);
                                finish();
                            }else {
                                Log.e(TAG, "İSTEK GÖNDEREN TARAF" );
                                Log.e(TAG, "İSTEĞİ GÖNDEREN KİŞİ    :   "+lastUsername+"    İSTEĞİ ALAN TARAF   :   "+chatUsers.getToUserUsername() );
                                Intent ıntent = new Intent(ListUserActivity.this,MainActivity.class);
                                ıntent.putExtra(Constant.OTHERUSERNAME,chatUsers.getToUserUsername());
                                timer.cancel();
                                timer.purge();
                                startActivity(ıntent);
                                finish();
                            }
                            Log.e(TAG, "onClick: Line 531 userUsername is  " + chatUsers.getUserUsername());
                            Log.e(TAG, "run: Line 531 toUserUsername is  "+chatUsers.getToUserUsername() );
                        }
                    }
                });

            }
        });

    }

}
