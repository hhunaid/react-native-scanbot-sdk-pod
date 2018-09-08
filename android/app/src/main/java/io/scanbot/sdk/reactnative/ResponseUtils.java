/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import android.view.View;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.List;

public class ResponseUtils {

    private static void messageJson(final String status, final String msg, final Callback callback) {
        if (callback != null) {
            final WritableMap response = Arguments.createMap();
            response.putString(status, msg);
            callback.invoke(response);
        }
    }

    public static void successMessageJson(final String msg, final Callback callback) {
        ResponseUtils.messageJson("result", msg, callback);
    }

    public static void errorMessageJson(final String msg, final Callback callback) {
        ResponseUtils.messageJson("error", msg, callback);
    }

    public static WritableArray writableStringArray(final List<String> array) {
        final WritableArray result = Arguments.createArray();
        for (final String item : array) {
            result.pushString(item);
        }
        return result;
    }

    public static void sendReactEvent(String eventName, WritableMap event, View view) {
        final ReactContext reactContext = (ReactContext)view.getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(view.getId(), eventName, event);
    }
}
