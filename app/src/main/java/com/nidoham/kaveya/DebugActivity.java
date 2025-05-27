package com.nidoham.kaveya;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nidoham.kaveya.databinding.ActivityDebugBinding;
import com.nidoham.kaveya.firebase.google.analysis.CrashReport;

public class DebugActivity extends AppCompatActivity {
    private static final String TAG = "DebugActivity";
    private static final String CRASH_REF_PATH = "CRASH_MESSAGE";
    private static final String ERROR_EXTRA = "CRASH_ERROR";
    private static final String DEVICE_INFO_EXTRA = "DEVICE_INFO";

    private ActivityDebugBinding binding;
    private DatabaseReference crashReportsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDebugBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Toolbar
        setSupportActionBar(binding.topAppBar);
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        // Initialize Firebase
        try {
            crashReportsRef = FirebaseDatabase.getInstance().getReference(CRASH_REF_PATH);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Firebase initialization failed", e);
            showToast(R.string.firebase_not_initialized);
        }

        // Handle intent data
        String errorText = getIntent().getStringExtra(ERROR_EXTRA);
        String deviceInfo = getIntent().getStringExtra(DEVICE_INFO_EXTRA);
        binding.errorText.setText(errorText != null && !errorText.isEmpty()
                ? errorText
                : getString(R.string.unknown_error));

        // Setup buttons
        setupButtons(errorText, deviceInfo);
    }

    private void setupButtons(String errorText, String deviceInfo) {
        // Copy button
        binding.copyButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Error Details", errorText);
            clipboard.setPrimaryClip(clip);
            showToast(R.string.error_copied);
        });

        // Submit button
        binding.submit.setOnClickListener(v -> {
            if (!binding.submit.isEnabled()) return;
            binding.submit.setEnabled(false);
            submitCrashReport(errorText, deviceInfo);
        });
    }

    private void submitCrashReport(String errorText, String deviceInfo) {
        if (crashReportsRef == null) {
            showToast(R.string.firebase_not_initialized);
            binding.submit.setEnabled(true);
            return;
        }

        try {
            CrashReport report = new CrashReport.Builder()
                    .setThrowable(new Throwable(errorText != null && !errorText.isEmpty()
                            ? errorText
                            : "Unknown error"))
                    .setThreadName("unknown")
                    .setDeviceInfo(deviceInfo)
                    .build();

            crashReportsRef.push()
                    .setValue(report)
                    .addOnSuccessListener(aVoid -> {
                        showToast(R.string.report_submitted);
                        binding.submit.setEnabled(false);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to submit crash report", e);
                        showToast(R.string.report_submission_failed);
                        binding.submit.setEnabled(true);
                    });
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to create crash report", e);
            showToast(R.string.report_creation_failed);
            binding.submit.setEnabled(true);
        }
    }

    private void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    @Deprecated
    @MainThread
    @CallSuper
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Prevent memory leaks
    }
}