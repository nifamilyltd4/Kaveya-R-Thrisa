package com.nidoham.kaveya;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nidoham.kaveya.databinding.ActivitySplashBinding;
import com.nidoham.kaveya.liberies.SketchwareUtil;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;
    private static final int SPLASH_TIMEOUT = 2000; // 2 seconds
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private PermissionHandler permissionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Make status bar transparent and fix its position
        Window window = getWindow();
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        // Set status bar color
        window.setStatusBarColor(Color.TRANSPARENT);

        // Make status bar icons dark or light
        WindowInsetsControllerCompat windowInsetsController =
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        // Make content display edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false);
        setContentView(binding.getRoot());

        // Initialize PermissionHandler
        permissionHandler = new PermissionHandler(this, this, new PermissionHandler.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                proceedToNextActivity();
            }

            @Override
            public void onPermissionDenied() {
                SketchwareUtil.showMessage(SplashActivity.this, "Microphone permission required");
                finish();
            }
        });

      /*  // Check internet connection
        if (!SketchwareUtil.isConnected(this)) {
            SketchwareUtil.showMessage(this, "Please connect to internet");
            finish();
            return;
        } */

        // Check permission
        if (permissionHandler.checkMicrophonePermission()) {
            proceedToNextActivity();
        } else {
            permissionHandler.requestMicrophonePermission();
        }

        // Hide system bars
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    private void proceedToNextActivity() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                if (user != null) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, OnboardActivity.class));
                }
                finish();
            }
        }, SPLASH_TIMEOUT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHandler.handlePermissionResult(requestCode, permissions, grantResults);
    }

    public static class PermissionHandler {
        private final Context context;
        private final AppCompatActivity activity;
        private static final int PERMISSION_REQUEST_CODE = 100;
        private final PermissionCallback callback;

        public interface PermissionCallback {
            void onPermissionGranted();
            void onPermissionDenied();
        }

        public PermissionHandler(Context context, AppCompatActivity activity, PermissionCallback callback) {
            this.context = context;
            this.activity = activity;
            this.callback = callback;
        }

        public boolean checkMicrophonePermission() {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        }

        public void requestMicrophonePermission() {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        }

        public void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (callback != null) callback.onPermissionGranted();
                } else {
                    if (callback != null) callback.onPermissionDenied();
                }
            }
        }
    }
}