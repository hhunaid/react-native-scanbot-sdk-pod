/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import android.view.View;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.List;

public class ResponseUtils {

    public static void successMessageJson(final String msg, final Promise promise) {
        final WritableMap response = Arguments.createMap();
        response.putString("result", msg);
        promise.resolve(response);
    }

    public static void errorMessageJson(final String msg, final Promise promise) {
        final WritableMap response = Arguments.createMap();
        response.putString("error", msg);
        promise.reject(msg);
    }

    public static void sendReactEvent(String eventName, WritableMap event, View view) {
        final ReactContext reactContext = (ReactContext)view.getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(view.getId(), eventName, event);
    }
}
