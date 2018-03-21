package com.example.abdlkdr.wowzasample.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.abdlkdr.wowzasample.Model.User;
import com.example.abdlkdr.wowzasample.R;
import com.example.abdlkdr.wowzasample.Util.Constant;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.broadcast.WZBroadcast;
import com.wowza.gocoder.sdk.api.broadcast.WZBroadcastConfig;
import com.wowza.gocoder.sdk.api.configuration.WZMediaConfig;
import com.wowza.gocoder.sdk.api.devices.WZAudioDevice;
import com.wowza.gocoder.sdk.api.devices.WZCameraView;
import com.wowza.gocoder.sdk.api.errors.WZError;
import com.wowza.gocoder.sdk.api.errors.WZStreamingError;
import com.wowza.gocoder.sdk.api.status.WZState;
import com.wowza.gocoder.sdk.api.status.WZStatus;
import com.wowza.gocoder.sdk.api.status.WZStatusCallback;


import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements WZStatusCallback, View.OnClickListener {

    //telefon da ki yayın kadir
    //rtsp://172.20.10.2:1935/videochat/kadir
    //tablet melih
    //rtsp://172.20.10.2:1935/videochat/melih

    private VideoView videoView;

    // The top level GoCoder API interface
    private WowzaGoCoder goCoder;

    // The GoCoder SDK camera view
    private WZCameraView goCoderCameraView;

    // The GoCoder SDK audio device
    private WZAudioDevice goCoderAudioDevice;

    // The GoCoder SDK broadcaster
    private WZBroadcast goCoderBroadcaster;

    // The broadcast configuration settings
    private WZBroadcastConfig goCoderBroadcastConfig;

    private static final String TAG = "MainActivity";

    final User user = new User();

    // Properties needed for Android 6+ permissions handling
    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    private boolean mPermissionsGranted = true;
    private String[] mRequiredPermissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private String otherUsername = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        otherUsername = getIntentExtra(savedInstanceState);
        Log.e(TAG, "onCreate: otherUsername is   " + otherUsername + " My username  :   " + ListUserActivity.lastUsername);
        bindView();
        createClickStatus();
        setVideoView(otherUsername);
        existRequest();
//        getClickStatus(otherUsername);

        // Initialize the GoCoder SDK
        goCoder = WowzaGoCoder.init(getApplicationContext(), "GOSK-D544-0103-2550-6661-CF99");

        if (goCoder == null) {
            // If initialization failed, retrieve the last error and display it
            WZError goCoderInitError = WowzaGoCoder.getLastError();
            Toast.makeText(this,
                    "GoCoder SDK error: " + goCoderInitError.getErrorDescription(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Associate the WZCameraView defined in the U/I layout with the corresponding class member
        goCoderCameraView = (WZCameraView) findViewById(R.id.camera_preview);
        if (!goCoderCameraView.getCamera().isFront()) {
            goCoderCameraView.switchCamera();
        }
        goCoderCameraView.setZOrderMediaOverlay(true);
        goCoderCameraView.setZOrderOnTop(true);
        goCoderCameraView.setVisibility(View.VISIBLE);

        // Create an audio device instance for capturing and broadcasting audio
        goCoderAudioDevice = new WZAudioDevice();

        // Create a broadcaster instance
        goCoderBroadcaster = new WZBroadcast();
        // Create a configuration instance for the broadcaster
//        goCoderBroadcastConfig = new WZBroadcastConfig(WZMediaConfig.FRAME_SIZE_640x480);
        goCoderBroadcastConfig = new WZBroadcastConfig(WZMediaConfig.FRAME_SIZE_1280x720);

        // Set the connection properties for the target Wowza Streaming Engine server or Wowza Cloud account
        goCoderBroadcastConfig.setHostAddress("mobiversitewowza.westeurope.cloudapp.azure.com");
//        goCoderBroadcastConfig.setHostAddress("172.20.10.2");

        goCoderBroadcastConfig.setPortNumber(1935);
        goCoderBroadcastConfig.setApplicationName("videochat");
        goCoderBroadcastConfig.setStreamName(ListUserActivity.lastUsername);
//        goCoderBroadcastConfig.setStreamName(ListUserActivity.lastUsername);
//        goCoderBroadcastConfig.setStreamName("deneme");
        goCoderBroadcastConfig.setUsername("denemesource");
        goCoderBroadcastConfig.setPassword("Kadir1509");

        Log.e("Broadcast Config", "  Line 95" + goCoderBroadcastConfig.toString());

        // Designate the camera preview as the video broadcaster
        goCoderBroadcastConfig.setVideoBroadcaster(goCoderCameraView);

        // Designate the audio device as the audio broadcaster
        goCoderBroadcastConfig.setAudioBroadcaster(goCoderAudioDevice);

        // Associate the onClick() method as the callback for the broadcast button's click event
        Button broadcastButton = (Button) findViewById(R.id.broadcast_button);
        broadcastButton.setOnClickListener(this);
        startCamertaView();
    }

    @Override
    public void onBackPressed() {
        goCoderCameraView.stopPreview();
        if (goCoderBroadcaster.getStatus().isRunning()){
            goCoderBroadcaster.endBroadcast(this);
        }
        deleteRequestLiveChat(ListUserActivity.lastUsername);
        setUserStatus(Constant.ONLINE);
        Intent ıntent = new Intent(MainActivity.this,ListUserActivity.class);
        ıntent.putExtra("username",ListUserActivity.lastUsername);
        startActivity(ıntent);
        finish();
    }

    //------------------------------------------------------------------------------------------------------------------------//

    private void startCamertaView(){
        // Ensure the minimum set of configuration settings have been specified necessary to
        // initiate a broadcast streaming session
        WZStreamingError configValidationError = goCoderBroadcastConfig.validateForBroadcast();

        if (configValidationError != null) {
            Toast.makeText(this, configValidationError.getErrorDescription(), Toast.LENGTH_LONG).show();
        } else if (goCoderBroadcaster.getStatus().isRunning()) {
            // Stop the broadcast that is currently running
            goCoderBroadcaster.endBroadcast(this);
            setClickStatus("false");
        } else {
            // Start streaming
            setUserStatus(Constant.OFFLINE);
            goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, this);
            setClickStatus("true");
        }
    }

    // Called when an activity is brought to the foreground
    @Override
    protected void onResume() {
        super.onResume();

        // Android 6 (Marshmallow) or above,
        // have been granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPermissionsGranted = hasPermissions(this, mRequiredPermissions);
            if (!mPermissionsGranted)
                ActivityCompat.requestPermissions(this, mRequiredPermissions, PERMISSIONS_REQUEST_CODE);
        } else
            mPermissionsGranted = true;

        // Start the camera preview display
        if (mPermissionsGranted && goCoderCameraView != null) {
            if (goCoderCameraView.isPreviewPaused())
                goCoderCameraView.onResume();
            else
                goCoderCameraView.startPreview();

        }

    }

    // The results of the permissions request
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        mPermissionsGranted = true;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // Check the result of each permission granted
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mPermissionsGranted = false;
                    }
                }
            }
        }
    }

    // Utility method to check the status of a permissions request for an array of permission identifiers
    private static boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions)
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        return true;
    }

    // The callback invoked when the broadcast button is pressed
    @Override
    public void onClick(View view) {
//        // return if the user hasn't granted the app the necessary permissions
//        if (!mPermissionsGranted) return;
//        // Ensure the minimum set of configuration settings have been specified necessary to
//        // initiate a broadcast streaming session
//        WZStreamingError configValidationError = goCoderBroadcastConfig.validateForBroadcast();
//
//        if (configValidationError != null) {
//            Toast.makeText(this, configValidationError.getErrorDescription(), Toast.LENGTH_LONG).show();
//        } else if (goCoderBroadcaster.getStatus().isRunning()) {
//            // Stop the broadcast that is currently running
//            goCoderBroadcaster.endBroadcast(this);
//            setClickStatus("false");
//        } else {
//            // Start streaming
//            setUserStatus(Constant.OFFLINE);
//            goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, this);
//            setClickStatus("true");
//        }
    }

    // The callback invoked upon changes to the state of the steaming broadcast
    @Override
    public void onWZStatus(final WZStatus goCoderStatus) {
        // A successful status transition has been reported by the GoCoder SDK
        final StringBuffer statusMessage = new StringBuffer("Broadcast status: ");

        switch (goCoderStatus.getState()) {
            case WZState.STARTING:
                statusMessage.append("Broadcast initialization");
                break;

            case WZState.READY:
                statusMessage.append("Ready to begin streaming");
                break;

            case WZState.RUNNING:
                statusMessage.append("Streaming is active");
//                setVideoView();
                break;

            case WZState.STOPPING:
                statusMessage.append("Broadcast shutting down");
                break;

            case WZState.IDLE:
                statusMessage.append("The broadcast is stopped");
                break;

            default:
                return;
        }

        // Display the status message using the U/I thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, statusMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // The callback invoked when an error occurs during a broadcast
    @Override
    public void onWZError(final WZStatus goCoderStatus) {
        // If an error is reported by the GoCoder SDK, display a message
        // containing the error details using the U/I thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,
                        "Streaming error: " + goCoderStatus.getLastError().getErrorDescription(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // Enable Android's sticky immersive full-screen mode
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        if (rootView != null)
            rootView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    //------------------------------------------------------------------------------------------------------------------------//

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
                Log.e(TAG, "onResponse: setUserStatus : "+status );

            }
        });
    }

    //The camera view display live stream video
    private void setVideoView(final String toUser) {
        try {

            String liveChatUrl = "rtsp://mobiversitewowza.westeurope.cloudapp.azure.com:1935/videochat/" + toUser;
            Log.d(TAG, "run: setVideoView URL    :   " + liveChatUrl);
            videoView.setVideoURI(Uri.parse(liveChatUrl));
            videoView.start();
            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    setVideoView(toUser);
                    Log.e(TAG, "Error playing video 353");
                    return true;
                }
            });


        } catch (Exception e) {
            setVideoView(toUser);
            e.printStackTrace();
        }
    }

    private void bindView() {

        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setZOrderMediaOverlay(true);
        videoView.setZOrderOnTop(true);
        videoView.setVisibility(View.VISIBLE);
        // Associate the WZCameraView defined in the U/I layout with the corresponding class member
        goCoderCameraView = (WZCameraView) findViewById(R.id.camera_preview);

    }

    //Creating for button click
    private void createClickStatus() {
        if (ListUserActivity.lastUsername != null) {
            Log.e(TAG, "createClickStatus: if statment ");
            OkHttpClient client = new OkHttpClient();
            HttpUrl url = new HttpUrl.Builder()
                    .scheme("http")
                    .host(Constant.SYSTEMIP)
                    .port(8080)
                    .addPathSegment("createClickStatus")
                    .addQueryParameter("user", ListUserActivity.lastUsername)
                    .build();
            Request request = new Request.Builder()
                    .url(url.toString())
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    Log.e(TAG, "onResponse: CreateClickStatus");
                }
            });
        } else {
            Log.e(TAG, "setClickStatus: Else statment kullanıcının kendi user ismini bulamadık ");
        }
    }

    //Setting buttun state
    private void setClickStatus(String isClick){
        if (ListUserActivity.lastUsername !=null){
            OkHttpClient client = new OkHttpClient();
            HttpUrl url = new HttpUrl.Builder()
                    .scheme("http")
                    .host(Constant.SYSTEMIP)
                    .port(8080)
                    .addPathSegment("setClickStatus")
                    .addQueryParameter("user", ListUserActivity.lastUsername)
                    .addQueryParameter("isClick",isClick)
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
        }else {
            Log.e(TAG, "setClickStatus: Else statment kullanıcının kendi user ismini bulamadık " );
        }

    }

    //If click is true then setting live chat
    private void getClickStatus(final String toUSer){
        if (otherUsername!=null){
            Log.e(TAG, "getClickStatus:  username is "  + toUSer );
            OkHttpClient client = new OkHttpClient();
            HttpUrl url = new HttpUrl.Builder()
                    .scheme("http")
                    .host(Constant.SYSTEMIP)
                    .port(8080)
                    .addPathSegment("getClickStatus")
                    .addQueryParameter("toUser", toUSer)
                    .build();
            Request request = new Request.Builder().url(url.toString()).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }
                @Override
                public void onResponse(final Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        final String myResponse = response.body().string();
                        @Override
                        public void run() {
                            if (response.body()!=null){
                                Timer timer = new Timer();
                                if (myResponse.contentEquals("true")){
                                    Log.e(TAG, "run: before setVideoView" );
                                    timer.cancel();
                                    timer.purge();
                                    setVideoView(toUSer);
                                }else {
                                    Log.e(TAG, "run: else statment");
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            getClickStatus(toUSer);
                                            Log.e(TAG, "run: DENEME TİMER" );
                                        }
                                    },2000);
                                }
                            }
                        }
                    });
                }
            });
        }else {
            Log.e(TAG, "getClickStatus: Fail line other username bulunamadı " );
        }

    }
    //İf accept the live chat make isAccepted trueq
    private void changeAcceptedStatus(String acceptedStatus) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(Constant.SYSTEMIP)
                .port(8080)
                .addPathSegment("changeAcceptedStatus")
                .addQueryParameter("toUser", ListUserActivity.lastUsername)
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

    private void deleteRequestLiveChat(String username){
        OkHttpClient client = new OkHttpClient();
        // http://10.106.148.11:8080/deleteRequestLiveChat?username=kadir
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(Constant.SYSTEMIP)
                .port(8080)
                .addPathSegment("deleteRequestLiveChat")
                .addQueryParameter("username", username)
                .build();
        Request request = new Request.Builder().url(url.toString()).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "onFailure: 567" );
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.e(TAG, "onResponse: " );
            }
        });

    }

    //Getting other user username
    private String getIntentExtra(Bundle savedInstanceState) {
        String username = "";
        if (savedInstanceState == null) {
            User user;
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                user = new User();
                return username;
            } else {
                username = extras.getString(Constant.OTHERUSERNAME);
                return username;
            }
        }
        return username;
    }

    //Check any request comes
    private void existRequest() {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(Constant.SYSTEMIP)
                .port(8080)
                .addPathSegment("existRequest")
                .addQueryParameter("username", ListUserActivity.lastUsername)
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
                                        new Timer().schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                existRequest();
                                            }
                                        },3000);
                                    } else {
                                        setUserStatus(Constant.ONLINE);
                                        Intent ıntent = new Intent(MainActivity.this,ListUserActivity.class);
                                        ıntent.putExtra("username",ListUserActivity.lastUsername);
                                        startActivity(ıntent);
                                    }
                                }
                            }
                        });
                    }
                });
    }

    //------------------------------------------------------------------------------------------------------------------------//


}
