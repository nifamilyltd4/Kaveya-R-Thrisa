package com.nidoham.kaveya;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nidoham.kaveya.firebase.google.analysis.CrashReport;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AdministrationLogger extends Application {
    private static final String TAG = "AdministrationLogger";
    private static final String CRASH_REF_PATH = "crashes";

    private static AdministrationLogger instance;
    private FirebaseDatabase firebaseDatabase;
    private ExecutorService executorService;
    private volatile boolean isCrashHandling = false;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        executorService = Executors.newSingleThreadExecutor();

        initializeFirebase();
        setupCrashHandler();
    }

    private void initializeFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
            }
            if (firebaseDatabase == null) {
                synchronized (this) {
                    if (firebaseDatabase == null) {
                        firebaseDatabase = FirebaseDatabase.getInstance();
                        firebaseDatabase.setPersistenceEnabled(true);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed", e);
        }
    }

    private void setupCrashHandler() {
        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            handleUncaughtException(thread, throwable);
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        });
    }

    private void handleUncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        if (isCrashHandling) return;
        isCrashHandling = true;

        try {
            Log.e(TAG, "Crash in thread: " + thread.getName(), throwable);
            logCrashToFirebase(throwable, thread.getName());
            showDebugScreen(throwable);
        } catch (Exception e) {
            Log.e(TAG, "Crash handling error", e);
        }
    }

    private void logCrashToFirebase(Throwable throwable, String threadName) {
        if (firebaseDatabase == null) {
            Log.e(TAG, "Firebase not initialized, can't log crash");
            return;
        }

        executorService.execute(() -> {
            try {
                CrashReport report = new CrashReport.Builder()
                        .setThrowable(throwable)
                        .setThreadName(threadName)
                        .setDeviceInfo(getDeviceInfo())
                        .build();

                DatabaseReference crashRef = firebaseDatabase.getReference(CRASH_REF_PATH).push();
                crashRef.setValue(report, (error, ref) -> {
                    if (error != null) {
                        Log.e(TAG, "Crash log failed", error.toException());
                    } else {
                        Log.i(TAG, "Crash logged successfully");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error logging crash to Firebase", e);
            }
        });
    }

    private void showDebugScreen(Throwable throwable) {
        try {
            Intent intent = new Intent(this, DebugActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("error", Log.getStackTraceString(throwable));
            intent.putExtra("deviceInfo", getDeviceInfo());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start DebugActivity", e);
        }
    }

    private String getDeviceInfo() {
        try {
            return String.format("Device: %s %s\nAndroid: %s (API %d)\nBuild: %s\nApp: %s",
                    Build.MANUFACTURER,
                    Build.MODEL,
                    Build.VERSION.RELEASE,
                    Build.VERSION.SDK_INT,
                    Build.FINGERPRINT,
                    com.nidoham.kaveya.BuildConfig.VERSION_NAME);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get device info", e);
            return "Device info unavailable";
        }
    }

    public static AdministrationLogger getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AdministrationLogger not initialized");
        }
        return instance;
    }

    public static Context getAppContext() {
        if (instance == null) {
            throw new IllegalStateException("AdministrationLogger not initialized");
        }
        return instance.getApplicationContext();
    }

    @Override
    public void onTerminate() {
        try {
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                executorService.awaitTermination(2, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Executor shutdown interrupted", e);
        }
        super.onTerminate();
    }
}
