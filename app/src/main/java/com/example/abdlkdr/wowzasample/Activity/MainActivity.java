package com.example.abdlkdr.wowzasample.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.abdlkdr.wowzasample.Model.User;
import com.example.abdlkdr.wowzasample.R;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements WZStatusCallback,View.OnClickListener, SurfaceHolder.Callback {

    //Camera hardware
    private Camera camera;

    //Holder for the cameraview
    private SurfaceHolder surfaceHolder;

    //Check stream ıs active
    public boolean streamIsActive= false;

    private VideoView videoView,otherUserView;

    //For camera view using surfaceview
    private SurfaceView surfaceView;
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

    private static final String TAG="MainActivity";

    // Properties needed for Android 6+ permissions handling
    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    private boolean mPermissionsGranted = true;
    private String[] mRequiredPermissions = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    final User user = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindView();
        getIntentExtra(savedInstanceState);
//        setSurfaceHolder();
//        setOtherUserView();
        setVideoView();
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
        if (!goCoderCameraView.getCamera().isFront()){
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
        goCoderBroadcastConfig = new WZBroadcastConfig(WZMediaConfig.FRAME_SIZE_1920x1080);

        // Set the connection properties for the target Wowza Streaming Engine server or Wowza Cloud account
        goCoderBroadcastConfig.setHostAddress("172.20.10.2");
//        goCoderBroadcastConfig.setHostAddress("10.106.148.14");
        goCoderBroadcastConfig.setPortNumber(1935);
        goCoderBroadcastConfig.setApplicationName("videochatm");
        goCoderBroadcastConfig.setStreamName("myStream");
        goCoderBroadcastConfig.setUsername("denemesource");
        goCoderBroadcastConfig.setPassword("Kadir1509");

        Log.e("Broadcast Config","  Line 95"+goCoderBroadcastConfig.toString());

        // Designate the camera preview as the video broadcaster
        goCoderBroadcastConfig.setVideoBroadcaster(goCoderCameraView);

        // Designate the audio device as the audio broadcaster
        goCoderBroadcastConfig.setAudioBroadcaster(goCoderAudioDevice);

        // Associate the onClick() method as the callback for the broadcast button's click event
        Button broadcastButton = (Button) findViewById(R.id.broadcast_button);
        broadcastButton.setOnClickListener(this);
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
                username = extras.getString("username");
                getUserStatus(username);
                final String finalUsername = username;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Username  :   " + finalUsername, Toast.LENGTH_SHORT).show();
                    }
                });
                return finalUsername;
            }
        }
        return username;
    }

    private User getUserStatus(String username) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("10.106.148.12")
                .port(8080)
                .addPathSegment("getUserStatus")
                .addQueryParameter("username", username)
                .build();
        Log.e(TAG, "Line 123 :      url  :   " + url.toString());
        Request request = new Request.Builder()
                .url(url.toString())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "failure line 125");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.e(TAG, "Succes Line 129");

                String result = "";
                result = response.body().string();
                if (result.contentEquals("")) {
                    Log.e(TAG, "Kullanıcı içeriği boş");
                } else {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(result);
                        if (!jsonObject.isNull("id")) {
                            user.setId(jsonObject.getString("id"));
                        }
                        if (!jsonObject.isNull("username")) {
                            user.setUsername(jsonObject.getString("username"));
                        }
                        if (!jsonObject.isNull("status")) {
                            user.setStatus(jsonObject.getString("status"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return user;
    }

    private void controlRequestIsExist(){
        
    }

    //Change the status if status is online then change status to offline
    //İf status is offline change status to online
    private void setUserStatus(String username) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("10.106.148.12")
                .port(8080)
                .addPathSegment("setUserStatus")
                .addQueryParameter("username", username)
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

    private void setVideoView() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                boolean haandler = new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (streamIsActive == true) {
                            Log.e("handler ", "  stream ıs atcıve " + streamIsActive);
                            MediaController mediaController = new MediaController(getApplicationContext());
                            mediaController.setAnchorView(videoView);
                            Uri uri = Uri.parse(getResources().getString(R.string.play_live_broadcast));
                            videoView.setVideoURI(uri);
                            videoView.setMediaController(mediaController);
                            videoView.requestFocus();
                            streamIsActive = false;
                            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    Log.e("asd", "asd");
                                    Log.e("Deneme ", "DEENEME");
                                    mp.start();
                                    return;
                                }
                            });
                        } else {
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    setVideoView();
                                }
                            }, 1000);
                        }
                    }
                }, 10000);


            }
        });


    }

    private void bindView() {
        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setZOrderMediaOverlay(true);
        videoView.setZOrderOnTop(true);
        videoView.setVisibility(View.VISIBLE);
        // Associate the WZCameraView defined in the U/I layout with the corresponding class member
        goCoderCameraView = (WZCameraView) findViewById(R.id.camera_preview);

//        otherUserView= (VideoView)findViewById(R.id.otherUserView);
//        otherUserView.setVisibility(View.VISIBLE);
//        otherUserView.setZOrderOnTop(true);
//        otherUserView.setZOrderMediaOverlay(true);

//        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
//        surfaceView.setVisibility(View.VISIBLE);
//        surfaceView.setZOrderMediaOverlay(true);
//        surfaceView.setZOrderOnTop(true);

    }

    // Called when an activity is brought to the foreground
    @Override
    protected void onResume() {
        super.onResume();

        // If running on Android 6 (Marshmallow) or above, check to see if the necessary permissions
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

    // Callback invoked in response to a call to ActivityCompat.requestPermissions() to interpret
    // the results of the permissions request
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
        Toast.makeText(MainActivity.this, "isim  Ç:  " + user.getUsername(), Toast.LENGTH_SHORT);
        Log.e(TAG, "username :   " + user.getUsername());

        // return if the user hasn't granted the app the necessary permissions
        if (!mPermissionsGranted) return;

        // Ensure the minimum set of configuration settings have been specified necessary to
        // initiate a broadcast streaming session
        WZStreamingError configValidationError = goCoderBroadcastConfig.validateForBroadcast();

        if (configValidationError != null) {
            Toast.makeText(this, configValidationError.getErrorDescription(), Toast.LENGTH_LONG).show();
        } else if (goCoderBroadcaster.getStatus().isRunning()) {
            // Stop the broadcast that is currently running
            goCoderBroadcaster.endBroadcast(this);
//            setUserStatus("kadir");
            setUserStatus(user.getUsername());
            Toast.makeText(MainActivity.this, "isim  Ç:  " + user.getUsername(), Toast.LENGTH_SHORT);
        } else {
            // Start streaming
//            setUserStatus("kadir");
            setUserStatus(user.getUsername());
            Toast.makeText(MainActivity.this, "isim  Ç:  " + user.getUsername(), Toast.LENGTH_SHORT);
            goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, this);
        }
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
                streamIsActive = true;

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

    //For the camera implements first creating camera
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //if you want to open front facing camera use this line
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        try {
            camera = Camera.open();

        } catch (Exception e) {
            System.out.println(e);
            Log.e("Line 388", "   ");
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
//        previewSizes.get(0).width=175;
//        previewSizes.get(0).height = 100;
        Camera.Size previewSize = previewSizes.get(0);
        for (int i = 0; i < previewSizes.size(); i++) {
            if (previewSizes.get(i).width > previewSize.width)
                previewSize = previewSizes.get(i);
        }
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//        parameters.set("rotation", 90);
//        parameters.setRotation(90);
        camera.setParameters(parameters);

        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
//        camera.startPreview();
        try {
            setCameraDisplayOrientation(MainActivity.this, cameraId, camera);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (RuntimeException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //For the camera implements if change camera view
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (surfaceHolder == null) {
            return;
        }
        try {
            camera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        try {
            setCameraDisplayOrientation(MainActivity.this, cameraId, camera);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {

        }
    }

    //For the camera implements for destroyed camera view
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    //Configure camera display rotation
    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }
}
