package com.nidoham.kaveya.permission;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

public class PermissionHandler {

    private final Context context;
    private final AppCompatActivity activity;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private final PermissionCallback callback;

    public interface PermissionCallback {
        void onPermissionGranted(String permission);
        void onPermissionDenied(String permission);
    }

    public PermissionHandler(Context context, AppCompatActivity activity, PermissionCallback callback) {
        this.context = context;
        this.activity = activity;
        this.callback = callback;
    }

    // Check if the RECORD_AUDIO permission is granted
    public boolean checkMicrophonePermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    // Check if the VIBRATE permission is granted
    public boolean checkVibratePermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED;
    }

    // Request RECORD_AUDIO permission
    public void requestMicrophonePermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_CODE);
    }

    // Request VIBRATE permission
    public void requestVibratePermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.VIBRATE},
                PERMISSION_REQUEST_CODE);
    }

    // Handle permission results for both RECORD_AUDIO and VIBRATE
    public void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (callback != null) {
                    callback.onPermissionGranted(permissions[0]);
                }
            } else {
                if (callback != null) {
                    callback.onPermissionDenied(permissions[0]);
                }
            }
        }
    }
}