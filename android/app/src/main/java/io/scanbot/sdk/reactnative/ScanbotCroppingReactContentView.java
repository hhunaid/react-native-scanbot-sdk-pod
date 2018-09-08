/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;

import net.doo.snap.lib.detector.ContourDetector;
import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.lib.detector.Line2D;
import net.doo.snap.ui.EditPolygonImageView;
import net.doo.snap.ui.MagnifierView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static io.scanbot.sdk.reactnative.ScanbotConstants.DEFAULT_EDGE_COLOR;
import static io.scanbot.sdk.reactnative.ScanbotConstants.DEFAULT_COMPRESSION_QUALITY;

public class ScanbotCroppingReactContentView extends FrameLayout {

    WeakReference<ScanbotCroppingReactContentView> weakView;

    class InitImageViewTask extends AsyncTask<Object, Void, InitImageResult> {

        @Override
        protected InitImageResult doInBackground(Object... params) {
            final Uri imageUri = (Uri) params[0];
            final List<PointF> polygonArg = (List<PointF>) params[1];

            Pair<List<Line2D>, List<Line2D>> linesPair = null;
            List<PointF> polygon = null; // detected polygon

            try {
                final ContourDetector detector = new ContourDetector();

                final Bitmap originalBitmap = BitmapHelper.loadImage(imageUri, context.getCurrentActivity());
                final Bitmap resizedBitmap = BitmapHelper.resizeImage(originalBitmap, 1000, 1000);

                context.getCurrentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // important! first set the image and then the detected polygon and lines!
                        editPolygonImageView.setImageBitmap(resizedBitmap);
                        editPolygonImageView.setRotation(rotationDegrees);
                        // set up the MagnifierView every time when editPolygonView is set with a new image.
                        scanbotMagnifierView.setupMagnifier(editPolygonImageView);
                    }
                });

                final DetectionResult detectionResult = detector.detect(resizedBitmap);
                linesPair = new Pair<List<Line2D>, List<Line2D>>(detector.getHorizontalLines(), detector.getVerticalLines());
                switch (detectionResult) {
                    case OK:
                    case OK_BUT_BAD_ANGLES:
                    case OK_BUT_TOO_SMALL:
                    case OK_BUT_BAD_ASPECT_RATIO:
                        polygon = detector.getPolygonF();
//                        debugLog("Detected polygon: " + polygon);
                        break;
                    default:
                        polygon = getDefaultPolygon();
                        break;
                }
            } catch (final Exception e) {
//                errorLog("Could not init polygon image view on image: " + imageUri, e);
            }

            return new InitImageResult(linesPair, polygon);
        }

        @Override
        protected void onPostExecute(final InitImageResult result) {
            context.getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    editPolygonImageView.setPolygon(result.polygonF);

                    if (result.linesPair != null) {
                        editPolygonImageView.setLines(result.linesPair.first, result.linesPair.second);
                    }
                }
            });
        }
    }


    class InitImageResult {
        final Pair<List<Line2D>, List<Line2D>> linesPair;
        final List<PointF> polygonF;

        InitImageResult(final Pair<List<Line2D>, List<Line2D>> linesPair,
                        final List<PointF> polygonF) {
            this.linesPair = linesPair;
            this.polygonF = polygonF;
        }
    }


    class ApplyEditChangesTask extends AsyncTask<Object, Void, ApplyEditChangesTaskResult> {

        @Override
        protected ApplyEditChangesTaskResult doInBackground(Object... params) {
            final Uri imageUri = (Uri) params[0];
            final List<PointF> polygon = (List<PointF>) params[1];
            final int quality = jpgQuality;

//            debugLog("Cropping/warping with polygon: " + polygon);

            if (!isCancelled()) {
                try {
                    final Bitmap originalBitmap = BitmapHelper.loadImage(imageUri, context.getCurrentActivity());
                    final Bitmap resultImg = BitmapHelper.cropAndWarpImage(originalBitmap, polygon, true);
                    final Uri resultImgUri = BitmapHelper.storeImage(BitmapHelper.rotateBitmap(resultImg, rotationDegrees), quality, context.getCurrentActivity());
                    return new ApplyEditChangesTaskResult(resultImgUri, polygon);
                } catch (final Exception e) {
//                    errorLog("Could not process changes on image", e);
                    WritableMap event = Arguments.createMap();
                    event.putString("error", e.getMessage());
                    ResponseUtils.sendReactEvent("changesApplied", event, weakView.get());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(final ApplyEditChangesTaskResult result) {
            if (!isCancelled() && result != null) {
                WritableMap event = Arguments.createMap();
                event.putString("imageFileUri", result.imageUri.toString());
                event.putArray("polygon", JSONUtils.sdkPolygonToWritableArray(result.polygon));
                ResponseUtils.sendReactEvent("changesApplied", event, weakView.get());
            }
        }
    }


    class ApplyEditChangesTaskResult {
        final Uri imageUri;
        final List<PointF> polygon;

        ApplyEditChangesTaskResult(final Uri imageUri, final List<PointF> polygon) {
            this.imageUri = imageUri;
            this.polygon = polygon;
        }
    }

    private final Executor executor = Executors.newSingleThreadExecutor();
    private ScanbotSDKWrapper sdkWrapper;

    private Uri imageUri;
    private EditPolygonImageView editPolygonImageView;
    private MagnifierView scanbotMagnifierView;
    private ProgressBar processImageProgressBar;

    private int edgeColor = DEFAULT_EDGE_COLOR;
    private int jpgQuality = DEFAULT_COMPRESSION_QUALITY;

    private int screenOrientation;
    private int rotationDegrees = 0;
    private long lastRotationEventTs = 0L;

    private ThemedReactContext context;

    private String imageFileUri = "";

    private View findViewById(final String id) {
        final int idInt = ResourcesUtils.getResId("id", id, context.getCurrentActivity());
        return findViewById(idInt);
    }

    public ScanbotCroppingReactContentView(ThemedReactContext context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        weakView = new WeakReference<>(this);

        LayoutInflater
                .from(context.getCurrentActivity())
                .inflate(R.layout.scanbot_edit_image_view, this, true);

        sdkWrapper = new ScanbotSDKWrapper(context.getCurrentActivity());

        editPolygonImageView = (EditPolygonImageView) findViewById("scanbotEditImageView");
        editPolygonImageView.setEdgeColor(edgeColor);
        editPolygonImageView.setEdgeWidth(7.0f);

        scanbotMagnifierView = (MagnifierView) findViewById("scanbotMagnifierView");

        processImageProgressBar = (ProgressBar) findViewById("processImageProgressBar");

        screenOrientation = getResources().getConfiguration().orientation;
    }

    public void applyEditChanges() {
        lockScreenOrientation();
        processImageProgressBar.setVisibility(View.VISIBLE);

        new ApplyEditChangesTask().executeOnExecutor(executor, imageUri, editPolygonImageView.getPolygon());
    }

    public void cancelEditChanges() {
        WritableMap event = Arguments.createMap();
        ResponseUtils.sendReactEvent("changesCanceled", event, weakView.get());
    }

    public void rotateImageClockwise() {
        if ((System.currentTimeMillis() - lastRotationEventTs) < 350) {
            return;
        }
        rotationDegrees += 90;
        editPolygonImageView.rotateClockwise();
        lastRotationEventTs = System.currentTimeMillis();
    }

    private void lockScreenOrientation() {
        if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            context.getCurrentActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            context.getCurrentActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private static ArrayList<PointF> getDefaultPolygon() {
        return new ArrayList<PointF>() {
            {
                add(new PointF(0, 0));
                add(new PointF(1, 0));
                add(new PointF(1f, 1f));
                add(new PointF(0, 1));
            }
        };
    }

    public void setImageFileUri(@Nullable String imageFileUri) {
        this.imageFileUri = imageFileUri;

        imageUri = Uri.parse(imageFileUri);
        new InitImageViewTask().executeOnExecutor(executor, imageUri, getDefaultPolygon());
    }

    public void setEdgeColor(@Nullable String color) {
        if (color != null) {
            edgeColor = Color.parseColor(color);
        } else {
            edgeColor = DEFAULT_EDGE_COLOR;
        }
        editPolygonImageView.setEdgeColor(edgeColor);
    }

    public void setQuality(int quality) {
        jpgQuality = quality;
    }
}
