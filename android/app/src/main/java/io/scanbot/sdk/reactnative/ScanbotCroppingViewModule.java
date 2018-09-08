/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class ScanbotCroppingViewModule extends ReactContextBaseJavaModule {

    public ScanbotCroppingViewModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "ScanbotCroppingViewModule";
    }

    @ReactMethod
    public void applyCroppingChanges() {
        this.getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ScanbotCroppingViewManager.croppingView != null) {
                    ScanbotCroppingViewManager.croppingView.applyEditChanges();
                }
            }
        });
    }

    @ReactMethod
    public void dismissCroppingChanges() {
        this.getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ScanbotCroppingViewManager.croppingView != null) {
                    ScanbotCroppingViewManager.croppingView.cancelEditChanges();
                }
            }
        });
    }

    @ReactMethod
    public void rotateImageClockwise() {
        this.getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ScanbotCroppingViewManager.croppingView != null) {
                    ScanbotCroppingViewManager.croppingView.rotateImageClockwise();
                }
            }
        });
    }

}
