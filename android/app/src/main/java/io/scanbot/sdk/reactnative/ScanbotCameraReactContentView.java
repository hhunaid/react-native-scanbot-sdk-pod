/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.bridge.LifecycleEventListener;

import net.doo.snap.camera.AutoSnappingController;
import net.doo.snap.camera.CameraOpenCallback;
import net.doo.snap.camera.ContourDetectorFrameHandler;
import net.doo.snap.camera.PictureCallback;
import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.ui.PolygonView;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static io.scanbot.sdk.reactnative.ScanbotConstants.ACTION_DISMISS_SB_CAMERA;
import static io.scanbot.sdk.reactnative.ScanbotConstants.DEFAULT_AUTOSNAPPING_SENSITIVITY;
import static io.scanbot.sdk.reactnative.ScanbotConstants.DEFAULT_EDGE_COLOR;
import static net.doo.snap.lib.detector.DetectionResult.ERROR_NOTHING_DETECTED;

public class ScanbotCameraReactContentView extends FrameLayout {

    WeakReference<ScanbotCameraReactContentView> weakView;

    class EventHandler implements LifecycleEventListener {

        @Override
        public void onHostResume() {
            cameraView.onResume();
        }

        @Override
        public void onHostPause() {
            cameraView.onPause();
        }

        @Override
        public void onHostDestroy() {
            if (broadcastReceiver != null) {
                context.unregisterReceiver(broadcastReceiver);
            }
        }
    }

    class ProcessTakenPictureResult {
        final Uri originalImgUri;
        final int imageOrientation;
        final Uri documentImgUri;
        final DetectionResult sdkDetectionResult;
        final List<PointF> polygonF;

        ProcessTakenPictureResult(final Uri originalImgUri,
                                  final int imageOrientation,
                                  final Uri documentImgUri,
                                  final DetectionResult sdkDetectionResult,
                                  final List<PointF> polygonF) {
            this.originalImgUri = originalImgUri;
            this.imageOrientation = imageOrientation;
            this.documentImgUri = documentImgUri;
            this.sdkDetectionResult = sdkDetectionResult;
            this.polygonF = polygonF;
        }
    }

    class ProcessTakenPictureTask extends AsyncTask<Object, Void, ProcessTakenPictureResult> {

        @Override
        protected ProcessTakenPictureResult doInBackground(Object... params) {
            final byte[] image = (byte[]) params[0];
            final int imageOrientation = (Integer) params[1];
            final Activity activity = context.getCurrentActivity();

            final int quality = jpgQuality;
            final int inSampleSize = sampleSize;

            DetectionResult sdkDetectionResult = ERROR_NOTHING_DETECTED;
            List<PointF> polygonF = Collections.emptyList();
            Uri originalImgUri = null, documentImgUri = null;
            try {
                // decode original image:
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = inSampleSize;
                Bitmap originalBitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);

                // rotate original image if required:
                if (imageOrientation > 0) {
                    final Matrix matrix = new Matrix();
                    matrix.setRotate(imageOrientation, originalBitmap.getWidth() / 2f, originalBitmap.getHeight() / 2f);
                    originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, false);
                }

                // store original image:
                originalImgUri = BitmapHelper.storeImage(originalBitmap, quality, activity);

                final DocumentDetectionResult docDetectionResult = ScanbotSDKHelper.detectDocument(originalBitmap, true);
                sdkDetectionResult = docDetectionResult.sdkDetectionResult;
                polygonF = docDetectionResult.polygon;

                if (docDetectionResult.documentImage != null) {
                    documentImgUri = BitmapHelper.storeImage(docDetectionResult.documentImage, quality, activity);
                    docDetectionResult.documentImage.recycle();
                } else {
                    WritableMap event = Arguments.createMap();
                    event.putString("imageFileUri", originalImgUri.toString());
                    ResponseUtils.sendReactEvent("imageCaptured", event, weakView.get());
                }
            } catch (final Exception e) {
//                errorLog("Could not process image: " + e.getMessage(), e);
                WritableMap event = Arguments.createMap();
                event.putString("error", e.getMessage());
                ResponseUtils.sendReactEvent("imageCaptureFailed", event, weakView.get());
            }

            return new ProcessTakenPictureResult(originalImgUri, imageOrientation,
                    documentImgUri, sdkDetectionResult, polygonF);
        }

        @Override
        protected void onPostExecute(final ProcessTakenPictureResult result) {
            if (!isCancelled()) {
                WritableMap event = Arguments.createMap();
                event.putString("imageFileUri", (result.documentImgUri != null ? result.documentImgUri.toString() : null));
                event.putString("originalImageFileUri", (result.originalImgUri != null ? result.originalImgUri.toString() : null));
                event.putArray("polygon", JSONUtils.sdkPolygonToWritableArray(result.polygonF));
                event.putString("detectionResult", JSONUtils.sdkDocDetectionResultToJsString(result.sdkDetectionResult));
                ResponseUtils.sendReactEvent("documentImageCaptured", event, weakView.get());

                context.getCurrentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        unlockScreenOrientation();
                        cameraView.continuousFocus();
                        cameraView.startPreview();
                        flashToggle.setVisibility(View.VISIBLE);
                        shutterDrawable.setActive(true);
                        snapImageButton.setVisibility(View.VISIBLE);
                        autosnapToggle.setVisibility(View.VISIBLE);
                        polygonView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    class ScanbotCameraBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals(ACTION_DISMISS_SB_CAMERA)) {
//                this.finish();
            }
        }

    }

    class PictureCallbackHandler implements PictureCallback {

        @Override
        public void onPictureTaken(byte[] image, int imageOrientation) {
            WritableMap event = Arguments.createMap();
            ResponseUtils.sendReactEvent("startCapturingImage", event, weakView.get());

            context.getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lockScreenOrientation();
                    resetUserGuidanceUi(true);
                    flashToggle.setVisibility(View.GONE);
                    shutterDrawable.setActive(false);
                    snapImageButton.setVisibility(View.GONE);
                    autosnapToggle.setVisibility(View.GONE);
                    polygonView.setVisibility(View.GONE);
                }
            });

            // process picture in a background task:
            new ProcessTakenPictureTask().executeOnExecutor(executor, image, imageOrientation);
        }

    }

    class FrameResultHandler implements ContourDetectorFrameHandler.ResultHandler {

        @Override
        public boolean handleResult(final ContourDetectorFrameHandler.DetectedFrame result) {
            //debugLog("Detection result: " + result.detectionResult);

            WritableMap event = Arguments.createMap();
            event.putString("status", result.detectionResult.toString());
            event.putArray("polygon", JSONUtils.sdkPolygonToWritableArray(result.polygon));
            ResponseUtils.sendReactEvent("polygonDetected", event, weakView.get());

            context.getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // this.detectionHelper.onResult(result.detectionResult);
                    showUserGuidance(result.detectionResult);
                }
            });

            return false;
        }

    }

    private final EventHandler eventHandler = new EventHandler();
    private final PictureCallbackHandler pictureCallbackHandler = new PictureCallbackHandler();
    private final FrameResultHandler frameResultHandler = new FrameResultHandler();

    private static final float SCALE_DEFAULT = 1f;
    private static final float TAKE_PICTURE_PRESSED_SCALE = 0.8f;
    private static final float TAKE_PICTURE_OVERSHOOT_TENSION = 8f;

    private static final long CAMERA_OPEN_DELAY_MS = 300L;

    private final Executor executor = Executors.newSingleThreadExecutor();

    private ScanbotSDKWrapper sdkWrapper;

    private int screenOrientation;

    private net.doo.snap.camera.ScanbotCameraView cameraView;
    private ContourDetectorFrameHandler contourDetectorFrameHandler;
    private PolygonView polygonView;
    private AutoSnappingController autoSnappingController;
    private Toast userGuidanceToast;
    private ImageView rotateDeviceImageView;
    private ImageButton snapImageButton;
    private ShutterDrawable shutterDrawable;
    private CheckBox flashToggle;
    private CheckBox autosnapToggle;

    private int drawable_ui_cam_rotation_v = 0,
            drawable_ui_cam_rotation_h = 0;

    private String autosnappingHintDoNotMove = "Don't move. Capturing document...";
    private String autosnappingHintMoveCloser = "Move closer.";
    private String autosnappingHintBadAngles = "Turn your device to\nhave a more rectangular outline.";
    private String autosnappingHintNothingDetected = "Searching for document...";
    private String autosnappingHintTooNoisy = "Background too noisy!\nSearching for document...";
    private String autosnappingHintTooDark = "Poor light!\nSearching for document...";

    private int edgeColor = DEFAULT_EDGE_COLOR;

    private int jpgQuality = ScanbotConstants.DEFAULT_COMPRESSION_QUALITY;

    private int sampleSize = 1; // 1 means original size (no downscale)

    private boolean autoSnappingEnabled = true;

    private double autoSnappingSensitivity = DEFAULT_AUTOSNAPPING_SENSITIVITY;

    private ScanbotCameraBroadcastReceiver broadcastReceiver;

    private ThemedReactContext context;

    private View findViewById(final String id) {
        final int idInt = ResourcesUtils.getResId("id", id, context.getCurrentActivity());
        return findViewById(idInt);
    }

    public ScanbotCameraReactContentView(ThemedReactContext context) {
        super(context);
        this.context = context;
        init();
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        post(measureAndLayout);
    }

    private final Runnable measureAndLayout = new Runnable() {
        @Override
        public void run() {
            measure(
                    MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
            layout(getLeft(), getTop(), getRight(), getBottom());
        }
    };

    private void init() {
        weakView = new WeakReference<>(this);

        LayoutInflater
                .from(context.getCurrentActivity())
                .inflate(R.layout.scanbot_camera_view, this, true);


        broadcastReceiver = new ScanbotCameraBroadcastReceiver();
        context.registerReceiver(broadcastReceiver, new IntentFilter(ACTION_DISMISS_SB_CAMERA));

        sdkWrapper = new ScanbotSDKWrapper(context.getCurrentActivity());

        drawable_ui_cam_rotation_v = ResourcesUtils.getResId("drawable", "ui_cam_rotation_v", context.getCurrentActivity());
        drawable_ui_cam_rotation_h = ResourcesUtils.getResId("drawable", "ui_cam_rotation_h", context.getCurrentActivity());

        setupUIButtons();

        cameraView = (net.doo.snap.camera.ScanbotCameraView) findViewById("scanbotCameraView");
        cameraView.setCameraOpenCallback(new CameraOpenCallback() {
            @Override
            public void onCameraOpened() {
                cameraView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cameraView.continuousFocus();
                    }
                }, CAMERA_OPEN_DELAY_MS);
            }
        });

        contourDetectorFrameHandler = ContourDetectorFrameHandler.attach(cameraView);

        polygonView = (PolygonView) findViewById("scanbotPolygonView");
        polygonView.setStrokeColor(edgeColor);
        polygonView.setStrokeWidth(7.0f); // not implemented in iOS SDK yet. so we use a fix value here.

        contourDetectorFrameHandler.addResultHandler(polygonView);
        contourDetectorFrameHandler.addResultHandler(frameResultHandler);

        autoSnappingController = AutoSnappingController.attach(cameraView, contourDetectorFrameHandler);
        autoSnappingController.setSensitivity((float) autoSnappingSensitivity);

        cameraView.addPictureCallback(pictureCallbackHandler);

        userGuidanceToast = Toast.makeText(context.getCurrentActivity(), "", Toast.LENGTH_SHORT);
        userGuidanceToast.setGravity(Gravity.CENTER, 0, 0);
        rotateDeviceImageView = (ImageView) findViewById("rotateDeviceImageView");

        screenOrientation = getResources().getConfiguration().orientation;

        setAutoSnapEnabled(autoSnappingEnabled);

        context.addLifecycleEventListener(eventHandler);
    }

    protected void setupUIButtons() {
        snapImageButton = (ImageButton) findViewById("snapImageButton");
        snapImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.takePicture(false);
            }
        });
        snapImageButton.setOnTouchListener(new View.OnTouchListener() {

            private final Interpolator downInterpolator = new DecelerateInterpolator();
            private final Interpolator upInterpolator = new OvershootInterpolator(TAKE_PICTURE_OVERSHOOT_TENSION);

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        snapImageButton.animate()
                                .scaleX(TAKE_PICTURE_PRESSED_SCALE)
                                .scaleY(TAKE_PICTURE_PRESSED_SCALE)
                                .setInterpolator(downInterpolator)
                                .start();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        snapImageButton.animate()
                                .scaleX(SCALE_DEFAULT)
                                .scaleY(SCALE_DEFAULT)
                                .setInterpolator(upInterpolator)
                                .start();
                        break;
                }

                return false;
            }
        });

        shutterDrawable = new ShutterDrawable(context);
        snapImageButton.setImageDrawable(shutterDrawable);

        flashToggle = (CheckBox) findViewById("flashToggle");
        flashToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                setFlashEnabled(checked);
            }
        });

        autosnapToggle = (CheckBox) findViewById("autosnapToggle");
        autosnapToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                setAutoSnapEnabled(checked);
            }
        });
    }

    private void lockScreenOrientation() {
        if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            context.getCurrentActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            context.getCurrentActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void unlockScreenOrientation() {
        context.getCurrentActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    private void resetUserGuidanceUi(boolean cancelToast) {
        rotateDeviceImageView.setVisibility(View.GONE);
        //moveCloser.setVisibility(View.GONE);
        shutterDrawable.setActive(false);

        if (cancelToast) {
            userGuidanceToast.cancel();
        }
    }


    private void showUserGuidance(final DetectionResult result) {
        if (!isAutoSnapEnabled()) {
            return;
        }

        resetUserGuidanceUi(false);
        switch (result) {
            case OK:
                userGuidanceToast.setText(autosnappingHintDoNotMove);
                userGuidanceToast.show();
                shutterDrawable.setActive(true);
                break;
            case OK_BUT_BAD_ASPECT_RATIO:
                userGuidanceToast.cancel();
                final int imageResource = (screenOrientation == Configuration.ORIENTATION_LANDSCAPE ?
                        drawable_ui_cam_rotation_v : drawable_ui_cam_rotation_h);
                rotateDeviceImageView.setImageResource(imageResource);
                rotateDeviceImageView.setVisibility(View.VISIBLE);
                break;
            case OK_BUT_TOO_SMALL:
                userGuidanceToast.setText(autosnappingHintMoveCloser);
                userGuidanceToast.show();
                break;
            case OK_BUT_BAD_ANGLES:
                userGuidanceToast.setText(autosnappingHintBadAngles);
                userGuidanceToast.show();
                break;
            case ERROR_NOTHING_DETECTED:
                userGuidanceToast.setText(autosnappingHintNothingDetected);
                userGuidanceToast.show();
                break;
            case ERROR_TOO_NOISY:
                userGuidanceToast.setText(autosnappingHintTooNoisy);
                userGuidanceToast.show();
                break;
            case ERROR_TOO_DARK:
                userGuidanceToast.setText(autosnappingHintTooDark);
                userGuidanceToast.show();
                break;
            default:
                userGuidanceToast.cancel();
                break;
        }
    }


    private boolean isAutoSnapEnabled() {
        return autoSnappingController.isEnabled();
    }

    protected void setFlashEnabled(final boolean enabled) {
        cameraView.useFlash(enabled);
    }

    public void setStrokeColor(String newColor) {
        edgeColor = Color.parseColor(newColor);
        polygonView.setStrokeColor(edgeColor);
    }

    public void setAutoSnapEnabled(boolean enabled) {
        resetUserGuidanceUi(true);

        autoSnappingController.setEnabled(enabled);
        contourDetectorFrameHandler.setEnabled(enabled);

        int image_resid = ResourcesUtils.getResId("drawable", "ui_scan_automatic_active", context);
        if (enabled) {
            shutterDrawable.startAnimation();
            polygonView.setVisibility(View.VISIBLE);
        } else {
            shutterDrawable.stopAnimation();
            polygonView.setVisibility(View.GONE);
            image_resid = ResourcesUtils.getResId("drawable", "ui_scan_automatic", context);
        }
        autosnapToggle.setBackgroundResource(image_resid);
    }

    public void setImageCompressionQuality(int quality) {
        jpgQuality = quality;
    }

    public void setAutoSnappingSensitivity(float sensitivity) {
        autoSnappingSensitivity = sensitivity;
    }

    public void setSampleSize(int size) {
        sampleSize = size;
    }

    public void setTextResBundle(ReadableMap textResBundle) {

        if (textResBundle.getString("autosnapping_hint_do_not_move") != null) {
            autosnappingHintDoNotMove = textResBundle.getString("autosnapping_hint_do_not_move");
        }

        if (textResBundle.getString("autosnapping_hint_move_closer") != null) {
            autosnappingHintMoveCloser = textResBundle.getString("autosnapping_hint_move_closer");
        }

        if (textResBundle.getString("autosnapping_hint_bad_angles") != null) {
            autosnappingHintBadAngles = textResBundle.getString("autosnapping_hint_bad_angles");
        }

        if (textResBundle.getString("autosnapping_hint_nothing_detected") != null) {
            autosnappingHintNothingDetected = textResBundle.getString("autosnapping_hint_nothing_detected");
        }

        if (textResBundle.getString("autosnapping_hint_too_noisy") != null) {
            autosnappingHintTooNoisy = textResBundle.getString("autosnapping_hint_too_noisy");
        }

        if (textResBundle.getString("autosnapping_hint_too_dark") != null) {
            autosnappingHintTooDark = textResBundle.getString("autosnapping_hint_too_dark");
        }

    }

}
