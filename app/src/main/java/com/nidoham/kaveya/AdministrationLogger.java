package com.nidoham.kaveya;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nidoham.kaveya.firebase.google.analysis.CrashReport;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdministrationLogger extends Application {
    private static final String TAG = "AdministrationLogger";
    private static final String CRASH_REF_PATH = "crashes";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/nifamilyltd4/Kaveya-R-Thrisa/issues";
    private static final String GITHUB_LABEL = "crash-report";
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_BODY_LENGTH = 65536; // GitHub issue body limit

    private static AdministrationLogger instance;
    private FirebaseDatabase firebaseDatabase;
    private ExecutorService executorService;
    private volatile boolean isCrashHandling = false;
    private final OkHttpClient httpClient;
    private final Gson gson = new Gson();

    public AdministrationLogger() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        executorService = Executors.newFixedThreadPool(2); // Increased thread pool

        initializeFirebase();
        setupCrashHandler();
        Log.i(TAG, "AdministrationLogger initialized successfully");
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
            Log.i(TAG, "Firebase initialized successfully");
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
        Log.i(TAG, "Crash handler setup completed");
    }

    private void handleUncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        if (isCrashHandling) {
            Log.w(TAG, "Already handling a crash, skipping");
            return;
        }
        isCrashHandling = true;

        try {
            Log.e(TAG, "Uncaught exception in thread: " + thread.getName(), throwable);
            
            // Log to Firebase (async)
            logCrashToFirebase(throwable, thread.getName());
            
            // Log to GitHub (async)
            logCrashToGitHub(throwable, thread.getName());
            
            // Show debug screen
            showDebugScreen(throwable);
            
        } catch (Exception e) {
            Log.e(TAG, "Error in crash handling", e);
        } finally {
            // Reset flag after a delay to prevent infinite loops
            executorService.execute(() -> {
                try {
                    Thread.sleep(5000); // 5 second delay
                    isCrashHandling = false;
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    private void logCrashToFirebase(Throwable throwable, String threadName) {
        if (firebaseDatabase == null) {
            Log.e(TAG, "Firebase not initialized, cannot log crash");
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
                        Log.e(TAG, "Failed to log crash to Firebase", error.toException());
                    } else {
                        Log.i(TAG, "Crash logged to Firebase successfully");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error logging crash to Firebase", e);
            }
        });
    }

    private void logCrashToGitHub(Throwable throwable, String threadName) {
        // Validate GitHub token
        String githubToken = BuildConfig.GITHUB_TOKEN;
        if (githubToken == null || githubToken.trim().isEmpty()) {
            Log.e(TAG, "GitHub token not configured, skipping GitHub crash report");
            return;
        }

        executorService.execute(() -> {
            try {
                String stackTrace = getStackTraceString(throwable);
                String deviceInfo = getDeviceInfo();
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(new Date());

                // Create issue title
                String issueTitle = createIssueTitle(throwable);
                
                // Create issue body
                String issueBody = createIssueBody(throwable, threadName, stackTrace, deviceInfo, timestamp);

                // Create GitHub issue payload
                Map<String, Object> issueData = new HashMap<>();
                issueData.put("title", issueTitle);
                issueData.put("body", issueBody);
                issueData.put("labels", new String[]{GITHUB_LABEL, "bug", "auto-generated"});

                String jsonPayload = gson.toJson(issueData);
                
                // Send to GitHub
                sendToGitHub(jsonPayload);

            } catch (Exception e) {
                Log.e(TAG, "Error preparing GitHub crash report", e);
            }
        });
    }

    private String createIssueTitle(Throwable throwable) {
        String title = "Crash Report: ";
        
        if (throwable.getMessage() != null && !throwable.getMessage().trim().isEmpty()) {
            title += throwable.getMessage();
        } else {
            title += throwable.getClass().getSimpleName();
        }
        
        // Truncate if too long
        if (title.length() > MAX_TITLE_LENGTH) {
            title = title.substring(0, MAX_TITLE_LENGTH - 3) + "...";
        }
        
        return title;
    }

    private String createIssueBody(Throwable throwable, String threadName, String stackTrace, 
                                 String deviceInfo, String timestamp) {
        StringBuilder body = new StringBuilder();
        
        body.append("## 🐛 Crash Report\n\n");
        body.append("**Timestamp:** ").append(timestamp).append("\n");
        body.append("**Thread:** ").append(threadName).append("\n");
        body.append("**Exception:** ").append(throwable.getClass().getSimpleName()).append("\n\n");
        
        if (throwable.getMessage() != null) {
            body.append("### Error Message\n");
            body.append("```\n").append(throwable.getMessage()).append("\n```\n\n");
        }
        
        body.append("### Device Information\n");
        body.append("```\n").append(deviceInfo).append("\n```\n\n");
        
        body.append("### Stack Trace\n");
        body.append("```java\n").append(stackTrace).append("\n```\n\n");
        
        // Add cause if exists
        Throwable cause = throwable.getCause();
        if (cause != null) {
            body.append("### Root Cause\n");
            body.append("**Exception:** ").append(cause.getClass().getSimpleName()).append("\n");
            if (cause.getMessage() != null) {
                body.append("**Message:** ").append(cause.getMessage()).append("\n");
            }
            body.append("```java\n").append(getStackTraceString(cause)).append("\n```\n\n");
        }
        
        body.append("---\n");
        body.append("*This issue was automatically generated by the crash reporting system.*");
        
        // Truncate if too long
        String result = body.toString();
        if (result.length() > MAX_BODY_LENGTH) {
            result = result.substring(0, MAX_BODY_LENGTH - 100) + "\n\n... (truncated due to length limit)";
        }
        
        return result;
    }

    private void sendToGitHub(String jsonPayload) {
        try {
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"), 
                    jsonPayload
            );

            Request request = new Request.Builder()
                    .url(GITHUB_API_URL)
                    .header("Authorization", "Bearer " + BuildConfig.GITHUB_TOKEN)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("User-Agent", "Kaveya-Android-App")
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Crash report sent to GitHub successfully");
                    if (response.body() != null) {
                        String responseBody = response.body().string();
                        JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
                        if (responseJson.has("html_url")) {
                            Log.i(TAG, "GitHub issue created: " + responseJson.get("html_url").getAsString());
                        }
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    Log.e(TAG, "Failed to send crash report to GitHub. Status: " + response.code() + 
                             ", Message: " + response.message() + ", Body: " + errorBody);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error sending crash report to GitHub", e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error sending crash report to GitHub", e);
        }
    }

    private String getStackTraceString(Throwable throwable) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            pw.close();
            return sw.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error getting stack trace", e);
            return "Stack trace unavailable: " + e.getMessage();
        }
    }

    private void showDebugScreen(Throwable throwable) {
        try {
            Intent intent = new Intent(this, DebugActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("error", getStackTraceString(throwable));
            intent.putExtra("deviceInfo", getDeviceInfo());
            intent.putExtra("timestamp", System.currentTimeMillis());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start DebugActivity", e);
        }
    }

    private String getDeviceInfo() {
        try {
            return String.format(Locale.getDefault(),
                    "Manufacturer: %s\n" +
                    "Model: %s\n" +
                    "Android Version: %s (API %d)\n" +
                    "Build Fingerprint: %s\n" +
                    "App Version: %s (%d)\n" +
                    "Package: %s\n" +
                    "Available Memory: %d MB\n" +
                    "Total Memory: %d MB",
                    Build.MANUFACTURER,
                    Build.MODEL,
                    Build.VERSION.RELEASE,
                    Build.VERSION.SDK_INT,
                    Build.FINGERPRINT,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                    getPackageName(),
                    Runtime.getRuntime().freeMemory() / 1024 / 1024,
                    Runtime.getRuntime().totalMemory() / 1024 / 1024
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to get device info", e);
            return "Device information unavailable: " + e.getMessage();
        }
    }

    /**
     * Manually report an error to both Firebase and GitHub
     */
    public void reportError(String title, String message, Throwable throwable) {
        if (throwable != null) {
            Log.e(TAG, "Manual error report: " + title + " - " + message, throwable);
            logCrashToFirebase(throwable, Thread.currentThread().getName());
            logCrashToGitHub(throwable, Thread.currentThread().getName());
        } else {
            // Create a synthetic exception for manual reporting
            Exception syntheticException = new Exception(title + ": " + message);
            Log.e(TAG, "Manual error report: " + title + " - " + message);
            logCrashToFirebase(syntheticException, Thread.currentThread().getName());
            logCrashToGitHub(syntheticException, Thread.currentThread().getName());
        }
    }

    public static AdministrationLogger getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AdministrationLogger not initialized. Make sure it's declared in AndroidManifest.xml");
        }
        return instance;
    }

    public static Context getAppContext() {
        if (instance == null) {
            throw new IllegalStateException("AdministrationLogger not initialized. Make sure it's declared in AndroidManifest.xml");
        }
        return instance.getApplicationContext();
    }

    @Override
    public void onTerminate() {
        try {
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            }
            if (httpClient != null) {
                httpClient.dispatcher().executorService().shutdown();
                httpClient.connectionPool().evictAll();
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Executor shutdown interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Log.e(TAG, "Error during termination", e);
        }
        super.onTerminate();
    }
}