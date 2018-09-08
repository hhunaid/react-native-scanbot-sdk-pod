/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import android.graphics.Bitmap;
import android.graphics.PointF;

import net.doo.snap.lib.detector.DetectionResult;

import java.util.ArrayList;
import java.util.List;

public final class DocumentDetectionResult {
    public final DetectionResult sdkDetectionResult;
    public final List<PointF> polygon = new ArrayList<PointF>();
    public final Bitmap documentImage;

    public DocumentDetectionResult(final DetectionResult sdkDetectionResult,
                                   final List<PointF> polygon,
                                   final Bitmap documentImage) {
        this.sdkDetectionResult = sdkDetectionResult;
        if (polygon != null) {
            this.polygon.addAll(polygon);
        }
        this.documentImage = documentImage;
    }
}
