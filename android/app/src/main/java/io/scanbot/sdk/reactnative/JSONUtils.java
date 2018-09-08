/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import android.graphics.PointF;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import net.doo.snap.lib.detector.DetectionResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONUtils {

    private static final Map<DetectionResult, String> docDetectionResultMapping = new HashMap<DetectionResult, String>();
    static {
        docDetectionResultMapping.put(DetectionResult.OK,                       "OK");
        docDetectionResultMapping.put(DetectionResult.OK_BUT_BAD_ANGLES,        "OK_BUT_BAD_ANGLES");
        docDetectionResultMapping.put(DetectionResult.OK_BUT_BAD_ASPECT_RATIO,  "OK_BUT_BAD_ASPECT_RATIO");
        docDetectionResultMapping.put(DetectionResult.OK_BUT_TOO_SMALL,         "OK_BUT_TOO_SMALL");
        docDetectionResultMapping.put(DetectionResult.ERROR_TOO_DARK,           "ERROR_TOO_DARK");
        docDetectionResultMapping.put(DetectionResult.ERROR_TOO_NOISY,          "ERROR_TOO_NOISY");
        docDetectionResultMapping.put(DetectionResult.ERROR_NOTHING_DETECTED,   "ERROR_NOTHING_DETECTED");
    }

    public static String sdkDocDetectionResultToJsString(final DetectionResult detectionResult) {
        if (docDetectionResultMapping.containsKey(detectionResult)) {
            return docDetectionResultMapping.get(detectionResult);
        }
        return detectionResult.name();
    }

    public static WritableArray sdkPolygonToWritableArray(final List<PointF> polygon) {
        WritableArray points = Arguments.createArray();
        if (polygon != null) {
            for (final PointF p: polygon) {
                WritableMap pointRecord = Arguments.createMap();
                pointRecord.putDouble("x", p.x);
                pointRecord.putDouble("y", p.y);
                points.pushMap(pointRecord);
            }
        }
        return points;
    }
}
