package com.nidoham.kaveya;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.common.util.concurrent.RateLimiter;
import com.nidoham.kaveya.firebase.google.analysis.CrashReport;
import com.nidoham.kaveya.BuildConfig;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdministrationLogger extends Application {
    private static final String TAG = "CrashLogger";
    private static final String GITHUB_API_URL = BuildConfig.GITHUB_TOKEN; // Define in BuildConfig
    private static final String PREFS_NAME = "crash_logger_prefs";
    private static final String SENT_CRASHES_KEY = "sent_crashes";
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_BODY_LENGTH = 60000;

    private static AdministrationLogger instance;
    private FirebaseDatabase firebaseDatabase;
    private ExecutorService executor;
    private OkHttpClient httpClient;
    private SharedPreferences prefs;
    private final Set<String> sentCrashes = new CopyOnWriteArraySet<>();
    private final RateLimiter rateLimiter = RateLimiter.create(1.0); // 1 report per second
    private volatile boolean isHandlingCrash = false;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        initializeComponents();
        setupCrashHandler();
        sendPendingReports();

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "CrashLogger initialized");
        }
    }

    private void initializeComponents() {
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        executor = Executors.newFixedThreadPool(2);

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        sentCrashes.addAll(prefs.getStringSet(SENT_CRASHES_KEY, new HashSet<>()));

        initFirebase();
    }

    private void initFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
                if (FirebaseApp.getApps(this).isEmpty()) {
                    Log.e(TAG, "Firebase initialization failed: No FirebaseApp instance created");
                    return;
                }
            }
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.setPersistenceEnabled(true);
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Firebase initialized successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed", e);
        }
    }

    private void setupCrashHandler() {
        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            handleCrash(thread, throwable);
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        });
    }

    private void handleCrash(@NonNull Thread thread, @NonNull Throwable throwable) {
        if (isHandlingCrash || !rateLimiter.tryAcquire()) {
            Log.w(TAG, "Crash reporting skipped due to rate limiting or ongoing crash handling");
            return;
        }

        isHandlingCrash = true;
        Log.e(TAG, "Crash in thread: " + thread.getName(), throwable);

        try {
            String crashId = generateCrashId(throwable);

            if (sentCrashes.contains(crashId)) {
                Log.i(TAG, "Crash already reported: " + crashId);
                return;
            }

            storeCrashLocally(throwable, thread.getName(), crashId);

            executor.execute(() -> reportToFirebase(throwable, thread.getName()));
            executor.execute(() -> {
                if (reportToGitHub(throwable, thread.getName())) {
                    markAsSent(crashId);
                    removeStoredCrash(crashId);
                }
            });

            showDebugScreen(throwable);

        } catch (Exception e) {
            Log.e(TAG, "Error handling crash", e);
        } finally {
            isHandlingCrash = false;
        }
    }

    private String generateCrashId(Throwable throwable) {
        StringBuilder id = new StringBuilder()
                .append(throwable.getClass().getSimpleName())
                .append("_").append(System.currentTimeMillis());

        if (throwable.getMessage() != null) {
            id.append("_").append(throwable.getMessage().hashCode());
        }

        StackTraceElement[] stack = throwable.getStackTrace();
        for (int i = 0; i < Math.min(2, stack.length); i++) {
            id.append("_").append(stack[i].getClassName())
              .append(".").append(stack[i].getMethodName())
              .append(":").append(stack[i].getLineNumber());
        }

        return String.valueOf(id.toString().hashCode());
    }

    private void storeCrashLocally(Throwable throwable, String threadName, String crashId) {
        try {
            prefs.edit()
                 .putString("crash_" + crashId + "_class", throwable.getClass().getSimpleName())
                 .putString("crash_" + crashId + "_msg", throwable.getMessage() != null ? throwable.getMessage() : "")
                 .putString("crash_" + crashId + "_stack", getStackTrace(throwable))
                 .putString("crash_" + crashId + "_thread", threadName)
                 .putString("crash_" + crashId + "_time", String.valueOf(System.currentTimeMillis()))
                 .apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to store crash locally", e);
        }
    }

    private void markAsSent(String crashId) {
        sentCrashes.add(crashId);
        prefs.edit().putStringSet(SENT_CRASHES_KEY, sentCrashes).apply();
    }

    private void sendPendingReports() {
        executor.execute(() -> {
            try {
                Map<String, ?> allPrefs = prefs.getAll();
                for (String key : allPrefs.keySet()) {
                    if (key.startsWith("crash_") && key.endsWith("_class")) {
                        String crashId = key.substring(6, key.lastIndexOf("_class"));

                        if (!sentCrashes.contains(crashId)) {
                            if (sendStoredCrash(crashId)) {
                                markAsSent(crashId);
                                removeStoredCrash(crashId);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending pending reports", e);
            }
        });
    }

    private boolean sendStoredCrash(String crashId) {
        try {
            String baseKey = "crash_" + crashId + "_";
            String exceptionClass = prefs.getString(baseKey + "class", "");
            String message = prefs.getString(baseKey + "msg", "");
            String stackTrace = prefs.getString(baseKey + "stack", "");
            String timestamp = prefs.getString(baseKey + "time", "");

            if (exceptionClass.isEmpty() || stackTrace.isEmpty()) return false;

            return sendToGitHub(exceptionClass, message, stackTrace, timestamp);

        } catch (Exception e) {
            Log.e(TAG, "Error sending stored crash", e);
            return false;
        }
    }

    private void removeStoredCrash(String crashId) {
        String baseKey = "crash_" + crashId + "_";
        prefs.edit()
             .remove(baseKey + "class")
             .remove(baseKey + "msg")
             .remove(baseKey + "stack")
             .remove(baseKey + "thread")
             .remove(baseKey + "time")
             .apply();
    }

    private void reportToFirebase(Throwable throwable, String threadName) {
        if (firebaseDatabase == null) {
            Log.w(TAG, "Firebase database not initialized, skipping report");
            return;
        }

        try {
            CrashReport report = new CrashReport.Builder()
                    .setThrowable(throwable)
                    .setThreadName(threadName)
                    .setDeviceInfo(getDeviceInfo())
                    .build();

            firebaseDatabase.getReference("crashes").push()
                    .setValue(report, (error, ref) -> {
                        if (error != null) {
                            Log.e(TAG, "Firebase report failed", error.toException());
                        } else {
                            Log.i(TAG, "Firebase report sent");
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Firebase report error", e);
        }
    }

    private boolean reportToGitHub(Throwable throwable, String threadName) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
        return sendToGitHub(throwable.getClass().getSimpleName(),
                           throwable.getMessage(),
                           getStackTrace(throwable),
                           timestamp);
    }

    private boolean sendToGitHub(String exceptionClass, String message, String stackTrace, String timestamp) {
        String token = getGitHubToken();
        if (token == null || token.trim().isEmpty()) {
            Log.e(TAG, "GitHub token not configured");
            return false;
        }

        try {
            String title = createTitle(exceptionClass, message);
            String body = createBody(exceptionClass, message, stackTrace, timestamp);

            Map<String, Object> payload = new HashMap<>();
            payload.put("title", title);
            payload.put("body", body);
            payload.put("labels", new String[]{"crash-report", "bug", "auto-generated"});

            return postToGitHub(payload, token);

        } catch (Exception e) {
            Log.e(TAG, "GitHub report error", e);
            return false;
        }
    }

    private String createTitle(String exceptionClass, String message) {
        String title = "Crash: ";

        if (message != null && !message.trim().isEmpty()) {
            title += message;
        } else {
            title += exceptionClass;
        }

        String timeStamp = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(new Date());
        title += " (" + timeStamp + ")";

        return title.length() > MAX_TITLE_LENGTH ?
               title.substring(0, MAX_TITLE_LENGTH - 3) + "..." : title;
    }

    private String createBody(String exceptionClass, String message, String stackTrace, String timestamp) {
        StringBuilder body = new StringBuilder();
        body.append("## 🐛 Crash Report\n\n")
            .append("**Timestamp:** ").append(timestamp).append("\n")
            .append("**Exception:** ").append(exceptionClass).append("\n\n");

        if (message != null && !message.trim().isEmpty()) {
            body.append("### Error Message\n```\n").append(message).append("\n```\n\n");
        }

        body.append("### Device Info\n```\n").append(getDeviceInfo()).append("\n```\n\n")
            .append("### Stack Trace\n```java\n").append(stackTrace).append("\n```\n\n")
            .append("---\n*Auto-generated crash report*");

        String result = body.toString();
        return result.length() > MAX_BODY_LENGTH ?
               result.substring(0, MAX_BODY_LENGTH - 100) + "\n\n...(truncated)" : result;
    }

    private boolean postToGitHub(Map<String, Object> payload, String token) {
        try {
            String json = new Gson().toJson(payload);
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);

            Request request = new Request.Builder()
                    .url(GITHUB_API_URL)
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/vnd.github+json")
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "GitHub report sent successfully");
                    return true;
                } else if (response.code() == 403 || response.code() == 429) {
                    Log.e(TAG, "GitHub API rate limit or authentication error: " + response.code());
                    return false;
                } else {
                    Log.e(TAG, "GitHub report failed: " + response.code());
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "GitHub request error", e);
            return false;
        }
    }

    private String getGitHubToken() {
        try {
            if (BuildConfig.class.getField("GITHUB_TOKEN") != null) {
                return BuildConfig.GITHUB_TOKEN;
            }
            Log.e(TAG, "GitHub token not defined in BuildConfig");
            return null;
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "GitHub token not defined in BuildConfig", e);
            return null;
        }
    }

    private void showDebugScreen(Throwable throwable) {
        try {
            Intent intent = new Intent(this, DebugActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            intent.putExtra("CRASH_ERROR", getStackTrace(throwable));
            intent.putExtra("CRASH_MESSAGE", throwable.getMessage());
            intent.putExtra("CRASH_CLASS", throwable.getClass().getSimpleName());
            intent.putExtra("DEVICE_INFO", getDeviceInfo());
            intent.putExtra("TIMESTAMP", System.currentTimeMillis());
            intent.putExtra("THREAD_NAME", Thread.currentThread().getName());

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                Log.i(TAG, "Debug screen launched");
            } else {
                Log.e(TAG, "DebugActivity not found in manifest");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch debug screen", e);
        }
    }

    private String getStackTrace(Throwable throwable) {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "Stack trace unavailable: " + e.getMessage();
        }
    }

    private String getDeviceInfo() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long freeMemory = runtime.freeMemory() / 1024 / 1024;
            long totalMemory = runtime.totalMemory() / 1024 / 1024;
            long maxMemory = runtime.maxMemory() / 1024 / 1024;

            return String.format(Locale.getDefault(),
                    "Device: %s %s\n" +
                    "Android: %s (API %d)\n" +
                    "App: %s (%d)\n" +
                    "Memory: %d/%d/%d MB (free/total/max)",
                    Build.MANUFACTURER, Build.MODEL,
                    Build.VERSION.RELEASE, Build.VERSION.SDK_INT,
                    BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE,
                    freeMemory, totalMemory, maxMemory
            );
        } catch (Exception e) {
            return "Device info unavailable: " + e.getMessage();
        }
    }

    /**
     * Manually reports an error to Firebase and GitHub.
     *
     * @param title     The title of the error
     * @param message   The error message
     * @param throwable The associated throwable, or null if none
     */
    public void reportError(String title, String message, Throwable throwable) {
        executor.execute(() -> {
            try {
                Throwable error = throwable != null ? throwable : new Exception(title + ": " + message);
                String crashId = generateCrashId(error);

                if (sentCrashes.contains(crashId)) {
                    Log.i(TAG, "Manual error already reported");
                    return;
                }

                storeCrashLocally(error, Thread.currentThread().getName(), crashId);
                reportToFirebase(error, Thread.currentThread().getName());
                if (reportToGitHub(error, Thread.currentThread().getName())) {
                    markAsSent(crashId);
                    removeStoredCrash(crashId);
                }
            } catch (Exception e) {
                Log.e(TAG, "Manual report error", e);
            }
        });
    }

    /**
     * Returns the singleton instance of AdministrationLogger.
     *
     * @return The singleton instance
     * @throws IllegalStateException if the instance is not initialized
     */
    public static AdministrationLogger getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AdministrationLogger not initialized");
        }
        return instance;
    }

    /**
     * Returns the application context.
     *
     * @return The application context
     */
    public static Context getAppContext() {
        return getInstance().getApplicationContext();
    }

    @Override
    public void onTerminate() {
        try {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            }
            if (httpClient != null) {
                httpClient.dispatcher().executorService().shutdownNow();
                httpClient.connectionPool().evictAll();
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Termination interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Log.e(TAG, "Termination error", e);
        }
        super.onTerminate();
    }
}