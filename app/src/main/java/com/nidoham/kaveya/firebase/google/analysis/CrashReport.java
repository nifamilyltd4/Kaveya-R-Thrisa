package com.nidoham.kaveya.firebase.google.analysis;

import android.os.Build;
import androidx.annotation.NonNull;
import java.util.UUID;

public class CrashReport {
    private String id;
    private String threadName;
    private String stackTrace;
    private String deviceInfo;
    private long timestamp;
    private int androidVersion;
    private String deviceModel;

    private CrashReport() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.androidVersion = Build.VERSION.SDK_INT;
        this.deviceModel = Build.MODEL;
    }

    public static class Builder {
        private final CrashReport report;

        public Builder() {
            report = new CrashReport();
        }

        public Builder setThrowable(@NonNull Throwable throwable) {
            report.stackTrace = throwable.getStackTrace().toString();
            return this;
        }

        public Builder setThreadName(@NonNull String threadName) {
            report.threadName = threadName;
            return this;
        }

        public Builder setDeviceInfo(@NonNull String deviceInfo) {
            report.deviceInfo = deviceInfo;
            return this;
        }

        public CrashReport build() {
            validateReport();
            return report;
        }

        private void validateReport() {
            if (report.stackTrace == null || report.stackTrace.isEmpty()) {
                throw new IllegalStateException("Stack trace cannot be null or empty");
            }
            if (report.threadName == null) {
                report.threadName = "unknown";
            }
            if (report.deviceInfo == null) {
                report.deviceInfo = String.format("Device: %s, Android: %d",
                        report.deviceModel,
                        report.androidVersion);
            }
        }
    }

    // Getters
    public String getId() { return id; }
    public String getThreadName() { return threadName; }
    public String getStackTrace() { return stackTrace; }
    public String getDeviceInfo() { return deviceInfo; }
    public long getTimestamp() { return timestamp; }
    public int getAndroidVersion() { return androidVersion; }
    public String getDeviceModel() { return deviceModel; }
}