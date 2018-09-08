/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import static io.scanbot.sdk.reactnative.ScanbotConstants.DEFAULT_COMPRESSION_QUALITY;

public class ScanbotCroppingViewManager extends SimpleViewManager<ScanbotCroppingReactContentView> {

    public static ScanbotCroppingReactContentView croppingView = null;

    @Override
    public String getName() {
        return "ScanbotCroppingView";
    }

    @Override
    public ScanbotCroppingReactContentView createViewInstance(ThemedReactContext context) {
        ScanbotCroppingViewManager.croppingView = new ScanbotCroppingReactContentView(context);
        return ScanbotCroppingViewManager.croppingView;
    }

    @ReactProp(name = "imageFileUri")
    public void setImageFileUri(ScanbotCroppingReactContentView view, @Nullable String imageFileUri) {
        view.setImageFileUri(imageFileUri);
    }

    @ReactProp(name = "edgeColor")
    public void setEdgeColor(ScanbotCroppingReactContentView view, @Nullable String edgeColor) {
        view.setEdgeColor(edgeColor);
    }

    @ReactProp(name = "imageCompressionQuality", defaultInt = DEFAULT_COMPRESSION_QUALITY)
    public void setQuality(ScanbotCroppingReactContentView view, int quality) {
        view.setQuality(quality);
    }

    @Override
    public @Nullable
    Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(
                "changesApplied",
                MapBuilder.of("registrationName", "onChangesAppliedWithPolygon"),
                "changesCanceled",
                MapBuilder.of("registrationName", "onChangesCanceled")
        );
    }

}
