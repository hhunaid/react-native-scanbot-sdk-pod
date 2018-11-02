package io.scanbot.sdk.reactnative;

import io.scanbot.sdk.persistence.CameraImageFormat;

public class ScanbotSDKConfiguration {
    private boolean loggingEnabled = false;
    private String licenseKey;
    private int storageImageQuality = 80;
    private CameraImageFormat storageImageFormat = CameraImageFormat.JPG;

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public int getStorageImageQuality() {
        return storageImageQuality;
    }

    public void setStorageImageQuality(int storageImageQuality) {
        if (storageImageQuality > 100) {
            storageImageQuality = 100;
        } else if (storageImageQuality < 1) {
            storageImageQuality = 1;
        }
        this.storageImageQuality = storageImageQuality;
    }

    public CameraImageFormat getStorageImageFormat() {
        return storageImageFormat;
    }

    public void setStorageImageFormat(CameraImageFormat storageImageFormat) {
        this.storageImageFormat = storageImageFormat;
    }
}
