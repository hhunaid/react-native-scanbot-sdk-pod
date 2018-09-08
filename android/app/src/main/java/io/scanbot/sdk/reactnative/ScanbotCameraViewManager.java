/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import static io.scanbot.sdk.reactnative.ScanbotConstants.DEFAULT_AUTOSNAPPING_ENABLED;
import static io.scanbot.sdk.reactnative.ScanbotConstants.DEFAULT_AUTOSNAPPING_SENSITIVITY;
import static io.scanbot.sdk.reactnative.ScanbotConstants.DEFAULT_COMPRESSION_QUALITY;

public class ScanbotCameraViewManager extends SimpleViewManager<ScanbotCameraReactContentView> {

    @Override
    public String getName() {
        return "ScanbotCameraView";
    }

    @Override
    public ScanbotCameraReactContentView createViewInstance(ThemedReactContext context) {
        return new ScanbotCameraReactContentView(context);
    }

    @ReactProp(name = "edgeColor")
    public void setStrokeColor(ScanbotCameraReactContentView view, @Nullable String strokeColor) {
        view.setStrokeColor(strokeColor);
    }

    @ReactProp(name = "autoSnappingEnabled")
    public void setAutoSnappingEnabled(ScanbotCameraReactContentView view, @Nullable Boolean enabled) {
        if (enabled == null) {
            view.setAutoSnapEnabled(DEFAULT_AUTOSNAPPING_ENABLED);
        } else {
            view.setAutoSnapEnabled(enabled);
        }
    }

    @ReactProp(name = "imageCompressionQuality", defaultInt = DEFAULT_COMPRESSION_QUALITY)
    public void setImageCompressionQuality(ScanbotCameraReactContentView view, int quality) {
        view.setImageCompressionQuality(quality);
    }

    @ReactProp(name = "autoSnappingSensitivity", defaultFloat = (float) DEFAULT_AUTOSNAPPING_SENSITIVITY)
    public void setAutoSnappingSensitivity(ScanbotCameraReactContentView view, float sensitivity) {
        view.setAutoSnappingSensitivity(sensitivity);
    }

    @ReactProp(name = "sampleSize", defaultInt = 1)
    public void setSampleSize(ScanbotCameraReactContentView view, int size) {
        view.setSampleSize(size);
    }

    @ReactProp(name = "textResBundle")
    public void setTextResBundle(ScanbotCameraReactContentView view, ReadableMap textResBundle) {
        view.setTextResBundle(textResBundle);
    }

    @Override
    public @Nullable Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(
                "imageCaptured",
                MapBuilder.of("registrationName", "onImageCaptured"),
                "imageCaptureFailed",
                MapBuilder.of("registrationName", "onImageCaptureFailed"),
                "documentImageCaptured",
                MapBuilder.of("registrationName", "onDocumentImageCaptured"),
                "polygonDetected",
                MapBuilder.of("registrationName", "onPolygonDetected"),
                "startCapturingImage",
                MapBuilder.of("registrationName", "onStartCapturingImage")
        );
    }

}
