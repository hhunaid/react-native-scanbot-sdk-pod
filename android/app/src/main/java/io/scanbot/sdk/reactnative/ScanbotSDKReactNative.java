/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanbotSDKReactNative extends ReactContextBaseJavaModule {

    private ExecutorService threadPool;

    public ScanbotSDKReactNative(final ReactApplicationContext reactContext) {
        super(reactContext);

        this.threadPool = Executors.newCachedThreadPool();
    }

    protected ExecutorService getThreadPool() {
        return this.threadPool;
    }

    private List<String> RNArrayToStringsList(final ReadableArray rnArray) {
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < rnArray.size(); i++) {
            list.add(rnArray.getString(i));
        }
        return list;
    }

    private String getOptionValue(final ReadableMap options, final String optionName, final String defaultValue) {
        return options.hasKey(optionName) ? options.getString(optionName) : defaultValue;
    }

    private boolean getOptionValue(final ReadableMap options, final String optionName, final boolean defaultValue) {
        return options.hasKey(optionName) ? options.getBoolean(optionName) : defaultValue;
    }

    private int getOptionValue(final ReadableMap options, final String optionName, final int defaultValue) {
        return options.hasKey(optionName) ? options.getInt(optionName) : defaultValue;
    }

    private double getOptionValue(final ReadableMap options, final String optionName, final double defaultValue) {
        return options.hasKey(optionName) ? options.getDouble(optionName) : defaultValue;
    }

    private ReadableArray getOptionValue(final ReadableMap options, final String optionName) {
        return options.hasKey(optionName) ? options.getArray(optionName) : null;
    }

    private String getRequiredOptionStringValue(final ReadableMap options, final String optionName, final Callback errorCallback) {
        if (options.hasKey(optionName)) {
            final String value = options.getString(optionName);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }

        ResponseUtils.errorMessageJson("Missing required option '"+optionName+"'", errorCallback);
        return null;
    }

    private Integer getRequiredOptionIntegerValue(final ReadableMap options, final String optionName, final Callback errorCallback) {
        if (options.hasKey(optionName)) {
            return options.getInt(optionName);
        }

        ResponseUtils.errorMessageJson("Missing required option '"+optionName+"'", errorCallback);
        return null;
    }

    private ReadableArray getRequiredOptionArrayValue(final ReadableMap options, final String optionName, final Callback errorCallback) {
        if (options.hasKey(optionName)) {
            final ReadableArray arr = options.getArray(optionName);
            if (arr != null && arr.size() > 0) {
                return arr;
            }
        }

        ResponseUtils.errorMessageJson("Missing value(s) of '"+optionName+"' array option", errorCallback);
        return null;
    }

    private boolean checkRejectSDKInitialization(final Callback errorCallback) {
        if (!ScanbotSDKHelper.isSdkInitialized()) {
            ResponseUtils.errorMessageJson("Scanbot SDK is not initialized. Please call the initializeSDK() method first.", errorCallback);
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "ScanbotSDK";
    }

    @ReactMethod
    public void initializeSDK(final ReadableMap options, final Callback successCallback, final Callback errorCallback) {
        this.getThreadPool().execute(new Runnable() {
            public void run() {
                final String licenseKey = getOptionValue(options, "licenseKey", null);
                final boolean loggingEnabled = getOptionValue(options, "loggingEnabled", false);
                ScanbotSDKHelper.initializeSDK(licenseKey, loggingEnabled, getCurrentActivity(), successCallback, errorCallback);
            }
        });
    }

    @ReactMethod
    public void isLicenseValid(final Callback successCallback, final Callback errorCallback) {
        this.getThreadPool().execute(new Runnable() {
            public void run() {
                if (!checkRejectSDKInitialization(errorCallback)) { return; }

                final ScanbotSDKWrapper wrapper = new ScanbotSDKWrapper(getCurrentActivity());
                final WritableMap response = Arguments.createMap();
                response.putBoolean("isLicenseValid", ScanbotSDKHelper.isLicenseActive(wrapper.pack.scanbotSDK));
                successCallback.invoke(response);
            }
        });
    }

    @ReactMethod
    public void detectDocument(final ReadableMap options, final Callback successCallback, final Callback errorCallback) {
        this.getThreadPool().execute(new Runnable() {
            public void run() {
                if (!checkRejectSDKInitialization(errorCallback)) { return; }

                final String imageFileUri = getRequiredOptionStringValue(options, "imageFileUri", errorCallback);
                if (imageFileUri == null) {
                    // errorCallback was already invoked by getRequiredOptionStringValue() method
                    return;
                }

                final int imageCompressionQuality = getOptionValue(options, "imageCompressionQuality", ScanbotConstants.DEFAULT_COMPRESSION_QUALITY);

                try {
                    ScanbotSDKHelper.documentDetection(imageFileUri, imageCompressionQuality,
                            getCurrentActivity(), successCallback, errorCallback);
                }
                catch (final Exception e) {
                    ResponseUtils.errorMessageJson("Document detection failed: " + e.getMessage(), errorCallback);
                }
            }
        });
    }

    @ReactMethod
    public void applyImageFilter(final ReadableMap options, final Callback successCallback, final Callback errorCallback) {
        this.getThreadPool().execute(new Runnable() {
            public void run() {
                if (!checkRejectSDKInitialization(errorCallback)) { return; }

                final String imageFileUri = getRequiredOptionStringValue(options, "imageFileUri", errorCallback);
                if (imageFileUri == null) {
                    // errorCallback was already invoked by getRequiredOptionStringValue() method
                    return;
                }

                final String filterType = getRequiredOptionStringValue(options, "filterType", errorCallback);
                if (filterType == null) {
                    // errorCallback was already invoked by getRequiredOptionStringValue() method
                    return;
                }

                final int imageCompressionQuality = getOptionValue(options, "imageCompressionQuality", ScanbotConstants.DEFAULT_COMPRESSION_QUALITY);

                try {
                    ScanbotSDKHelper.applyImageFilter(imageFileUri, filterType, imageCompressionQuality,
                            getCurrentActivity(), successCallback, errorCallback);
                }
                catch (final Exception e) {
                    ResponseUtils.errorMessageJson("Image filtering failed: " + e.getMessage(), errorCallback);
                }
            }
        });
    }

    @ReactMethod
    public void getOCRConfigs(final Callback successCallback, final Callback errorCallback) {
        this.getThreadPool().execute(new Runnable() {
            public void run() {
                if (!checkRejectSDKInitialization(errorCallback)) { return; }

                try {
                    ScanbotSDKHelper.getOcrConfigs(getCurrentActivity(), successCallback, errorCallback);
                }
                catch (final Exception e) {
                    ResponseUtils.errorMessageJson("Could not get OCR configs: " + e.getMessage(), errorCallback);
                }
            }
        });
    }

    @ReactMethod
    public void performOCR(final ReadableMap options, final Callback successCallback, final Callback errorCallback) {
        this.getThreadPool().execute(new Runnable() {
            public void run() {
                if (!checkRejectSDKInitialization(errorCallback)) { return; }

                final ReadableArray imageFileUris = getRequiredOptionArrayValue(options, "imageFileUris", errorCallback);
                if (imageFileUris == null) {
                    // errorCallback was already invoked by getRequiredOptionArrayValue() method
                    return;
                }

                final ReadableArray languages = getRequiredOptionArrayValue(options, "languages", errorCallback);
                if (languages == null) {
                    // errorCallback was already invoked by getRequiredOptionArrayValue() method
                    return;
                }

                final String outputFormat = getOptionValue(options, "outputFormat", "PLAIN_TEXT");

                try {
                    ScanbotSDKHelper.performOCR(RNArrayToStringsList(imageFileUris),
                            RNArrayToStringsList(languages), outputFormat,
                            getCurrentActivity(), successCallback, errorCallback);
                }
                catch (final Exception e) {
                    ResponseUtils.errorMessageJson("Perform OCR failed: " + e.getMessage(), errorCallback);
                }
            }
        });
    }

    @ReactMethod
    public void createPDF(final ReadableMap options, final Callback successCallback, final Callback errorCallback) {
        this.getThreadPool().execute(new Runnable() {
            public void run() {
                if (!checkRejectSDKInitialization(errorCallback)) { return; }

                final ReadableArray imageFileUris = getRequiredOptionArrayValue(options, "imageFileUris", errorCallback);
                if (imageFileUris == null) {
                    // errorCallback was already invoked by getRequiredOptionArrayValue() method
                    return;
                }

                try {
                    ScanbotSDKHelper.createPDF(RNArrayToStringsList(imageFileUris),
                            getCurrentActivity(), successCallback, errorCallback);
                }
                catch (final Exception e) {
                    ResponseUtils.errorMessageJson("Create PDF failed: " + e.getMessage(), errorCallback);
                }
            }
        });
    }

    @ReactMethod
    public void rotateImage(final ReadableMap options, final Callback successCallback, final Callback errorCallback) {
        this.getThreadPool().execute(new Runnable() {
            public void run() {
                if (!checkRejectSDKInitialization(errorCallback)) { return; }

                final String imageFileUri = getRequiredOptionStringValue(options, "imageFileUri", errorCallback);
                if (imageFileUri == null) {
                    // errorCallback was already invoked by getRequiredOptionStringValue() method
                    return;
                }

                final Integer degrees = getRequiredOptionIntegerValue(options, "degrees", errorCallback);
                if (degrees == null) {
                    // errorCallback was already invoked by getRequiredOptionStringValue() method
                    return;
                }

                final int imageCompressionQuality = getOptionValue(options, "imageCompressionQuality", ScanbotConstants.DEFAULT_COMPRESSION_QUALITY);

                try {
                    ScanbotSDKHelper.rotateImage(imageFileUri, degrees, imageCompressionQuality,
                            getCurrentActivity(), successCallback, errorCallback);
                }
                catch (final Exception e) {
                    ResponseUtils.errorMessageJson("Rotate image failed: " + e.getMessage(), errorCallback);
                }
            }
        });
    }

    @ReactMethod
    public void cleanup(final Callback successCallback, final Callback errorCallback) {
        this.getThreadPool().execute(new Runnable() {
            public void run() {
                if (!checkRejectSDKInitialization(errorCallback)) { return; }

                try {
                    ScanbotSDKHelper.cleanup(getCurrentActivity(), successCallback, errorCallback);
                }
                catch (final Exception e) {
                    ResponseUtils.errorMessageJson("Cleanup failed: " + e.getMessage(), errorCallback);
                }
            }
        });
    }

}
