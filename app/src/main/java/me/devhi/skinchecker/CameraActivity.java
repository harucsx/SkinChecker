package me.devhi.skinchecker;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";
    private CameraView camera;
    private ActionProcessButton btnCapture;

    private final static String FACE_API_ENDPOINT = "https://eastasia.api.cognitive.microsoft.com/face/v1.0/detect?returnFaceId=true&returnFaceLandmarks=true&returnFaceAttributes=age,gender,headPose,smile,facialHair,glasses,emotion,hair,makeup,occlusion,accessories,blur,exposure,noise";
    private final static String FACE_API_OPT_FACE_ID = "";
    private final static String FACE_API_OPT_FACE_LANDMARKS = "";
    private final static String FACE_API_OPT_FACE_ATTR = "";
    private final static String FACE_API_SECRET_KEY = "c8a6bdad334b47cb834ce2345b6d5cc8";
    private final static int PERMISSIONS_REQUEST_CODE = 100;

    public final static int SUCCESS_CALL_FACE = 1000;
    public final static int FAILD_CALL_FACE = 9999;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        btnCapture = (ActionProcessButton) findViewById(R.id.btnCapture);
        camera = (CameraView) findViewById(R.id.camera);
        camera.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(final byte[] picture) {
                callFaceAPI(picture);
            }
        });


        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCapture.setEnabled(false);
                btnCapture.setProgress(1);
                camera.capturePicture();
            }
        });

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

            // API 23 이상이면 런타임 퍼미션 처리 필요
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                int hasCameraPermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA);
                int hasWriteExternalStoragePermission =
                        ContextCompat.checkSelfPermission(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (hasCameraPermission == PackageManager.PERMISSION_GRANTED
                        && hasWriteExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
                    ;// 이미 퍼미션을 가지고 있음
                } else {
                    // 퍼미션이 없으므로 요청
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_CODE);
                }
            } else {
                ;
            }

        } else {
            Toast.makeText(CameraActivity.this, "Camera not supported",
                    Toast.LENGTH_LONG).show();
        }
    }

    protected void callFaceAPI(byte[] picture) {
        Ion.with(getApplicationContext())
                .load(FACE_API_ENDPOINT)
                .setHeader("Content-Type", "application/octet-stream")
                .setHeader("Ocp-Apim-Subscription-Key", FACE_API_SECRET_KEY)
                .setByteArrayBody(picture)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String response) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("response", response);
                        setResult(SUCCESS_CALL_FACE, resultIntent);
                        finish();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.setFacing(Facing.FRONT);
        camera.setJpegQuality(100);
        camera.setPlaySounds(false);
        camera.getCaptureSize();

        camera.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER); // Tap to focus!
        camera.mapGesture(Gesture.LONG_TAP, GestureAction.CAPTURE); // Long tap to shoot!

        camera.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE && grandResults.length > 0) {

            int hasCameraPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);
            int hasWriteExternalStoragePermission =
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (hasCameraPermission == PackageManager.PERMISSION_GRANTED
                    && hasWriteExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {

                //이미 퍼미션을 가지고 있음
                // doRestart(this);
            } else {
                checkPermissions();
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        int hasCameraPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int hasWriteExternalStoragePermission =
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);

        boolean cameraRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA);
        boolean writeExternalStorageRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if ((hasCameraPermission == PackageManager.PERMISSION_DENIED && cameraRationale)
                || (hasWriteExternalStoragePermission == PackageManager.PERMISSION_DENIED
                && writeExternalStorageRationale))
            showDialogForPermission("피부 분석을 위해 권한을 허가하셔야합니다.");

        else if ((hasCameraPermission == PackageManager.PERMISSION_DENIED && !cameraRationale)
                || (hasWriteExternalStoragePermission == PackageManager.PERMISSION_DENIED
                && !writeExternalStorageRationale))
            showDialogForPermissionSetting("피부 분석을 위해 사진 촬영 등의 권한이 필요합니다.\n" +
                    "설정에서 확인 후 권한을 설정해주세요.");

        else if (hasCameraPermission == PackageManager.PERMISSION_GRANTED
                || hasWriteExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
            // active;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //퍼미션 요청
                ActivityCompat.requestPermissions(CameraActivity.this,
                        new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_CODE);
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    private void showDialogForPermissionSetting(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(myAppSettings);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }
}
