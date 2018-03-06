package com.example.abdlkdr.wowzasample.Activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.abdlkdr.wowzasample.Adapter.RecyclerViewUserAdapter;
import com.example.abdlkdr.wowzasample.Model.User;
import com.example.abdlkdr.wowzasample.R;
import com.squareup.okhttp.Callback;
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

    private RecyclerViewUserAdapter recyclerViewUserAdapter;

    public ArrayList<User> userArrayList = new ArrayList<>();

    private static final String getAllUserUrl = "http://10.106.148.13:8080/getAllUser";

    private static final String TAG = "ListUserActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userlist);
        bindView();
        getAllUser(savedInstanceState);
        getIntentExtra(savedInstanceState);
//        getAllUserEverySec();

    }

    private void bindView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    }

    private void getAllUser(final Bundle saveInstanceState) {
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
                final String username = getIntentExtra(saveInstanceState);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.body() != null) {
                            try {
                                JSONArray jsonArray = new JSONArray(myResponse);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    User user = new User();
                                    boolean isUsernameExist=false;
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    if (!jsonObject.isNull("status") ) {
                                        Log.e(TAG,"status line");
                                        if (jsonObject.getString("status").contentEquals("online")){
                                            user.setStatus(jsonObject.getString("status"));
                                            if (!jsonObject.isNull("id")) {
                                                user.setId(jsonObject.getString("id"));
                                            }
                                            if (!jsonObject.isNull("username")) {
                                                user.setUsername(jsonObject.getString("username"));
                                                if (user.getUsername().contentEquals(username)){
                                                    Log.e(TAG,"username :   "+username+"user model username :   "+user.getUsername());
                                                    isUsernameExist=true;
                                                    if (isUsernameExist==true){
                                                        userArrayList.remove(user);
                                                    }
                                                }
                                                Log.e(TAG,"bbbbbbbbb"+user.getUsername());
                                            }
                                        }
                                    }
                                    if ((user.getUsername() !=null) && isUsernameExist==false)
                                    userArrayList.add(user);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
//                        new Timer().schedule(new TimerTask() {
//                            @Override
//                            public void run() {
////                                Log.e(TAG,"DENEME");
//                                getAllUser();
//                            }
//                        },1000);
                        recyclerViewUserAdapter = new RecyclerViewUserAdapter(getApplicationContext(), userArrayList);
                        recyclerView.setAdapter(recyclerViewUserAdapter);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ListUserActivity.this);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setHasFixedSize(true);
//                        recyclerViewUserAdapter.notifyDataSetChanged();
                    }
                });

            }
        });

    }

    private String getIntentExtra(Bundle savedInstanceState) {
        String username="";
        if (savedInstanceState == null) {
            User user;
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                user = new User();
                return username;
            } else {
                 username= extras.getString("username");
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

    private void checkRequestIsExist(){
        String toUser;
        boolean isAccepted;

    }


}
