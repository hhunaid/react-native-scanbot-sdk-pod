/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import android.graphics.PointF;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import net.doo.snap.lib.detector.DetectionResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.scanbot.mrzscanner.model.MRZDocumentType;
import io.scanbot.mrzscanner.model.MRZField;
import io.scanbot.mrzscanner.model.MRZRecognitionResult;
import io.scanbot.sdk.persistence.Page;
import io.scanbot.sdk.process.ImageFilterType;

public final class JSONUtils {

    private JSONUtils() {}

    private static Map<MRZDocumentType, String> MRZ_DOCUMENTTYPE_MAPPING = new HashMap<MRZDocumentType, String>();
    static {
        MRZ_DOCUMENTTYPE_MAPPING.put(MRZDocumentType.Passport, "PASSPORT");
        MRZ_DOCUMENTTYPE_MAPPING.put(MRZDocumentType.TravelDocument, "TRAVEL_DOCUMENT");
        MRZ_DOCUMENTTYPE_MAPPING.put(MRZDocumentType.Visa, "VISA");
        MRZ_DOCUMENTTYPE_MAPPING.put(MRZDocumentType.IDCard, "ID_CARD");
        MRZ_DOCUMENTTYPE_MAPPING.put(MRZDocumentType.Undefined, "UNDEFINED");
    }


    public static String sdkDocDetectionResultToJsString(final DetectionResult detectionResult) {
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

    public static Page convertReactToNativePage(final ReadableMap obj) {
        final String pageId = obj.getString("pageId");

        final List<PointF> polygon = new ArrayList<>();
        for (Object pointObj : obj.getArray("polygon").toArrayList()) {
            Map<String, Object> pointMap = (Map<String, Object>) pointObj;
            polygon.add(new PointF(((Double)pointMap.get("x")).floatValue(), ((Double)pointMap.get("y")).floatValue()));
        }

        final String jsDetectionResult = obj.getString("detectionResult");
        final DetectionResult detectionResult = ((jsDetectionResult != null && !"".equals(jsDetectionResult)) ?
                DetectionResult.valueOf(jsDetectionResult) : DetectionResult.ERROR_NOTHING_DETECTED);

        final String jsFilter = obj.getString("filter");
        final ImageFilterType filter = ((jsFilter != null && !"".equals(jsFilter)) ?
                ImageFilterType.valueOf(jsFilter) : ImageFilterType.NONE);

        return new Page(pageId, polygon, detectionResult, filter);
    }

    public static WritableMap sdkMRZRecognitionResultToWritableMap(final MRZRecognitionResult mrzRecognitionResult) {
        final WritableMap result = Arguments.createMap();

        result.putBoolean("recognitionSuccessful", mrzRecognitionResult.recognitionSuccessful);
        result.putString("documentType", sdkMRZDocumentTypeToJsString(mrzRecognitionResult.travelDocType));
        result.putInt("checkDigitsCount", mrzRecognitionResult.checkDigitsCount);
        result.putInt("validCheckDigitsCount", mrzRecognitionResult.validCheckDigitsCount);

        final WritableArray fields = Arguments.createArray();
        for (final MRZField mrzField : mrzRecognitionResult.fields) {
            final WritableMap field = Arguments.createMap();
            field.putString("name", mrzField.name.toString());
            field.putString("value", mrzField.value);
            field.putDouble("confidence", mrzField.averageRecognitionConfidence);
            fields.pushMap(field);
        }
        result.putArray("fields", fields);

        return result;
    }

    public static String sdkMRZDocumentTypeToJsString(final MRZDocumentType docType) {
        if (MRZ_DOCUMENTTYPE_MAPPING.containsKey(docType)) {
            return MRZ_DOCUMENTTYPE_MAPPING.get(docType);
        }
        return MRZ_DOCUMENTTYPE_MAPPING.get(MRZDocumentType.Undefined);
    }
}
