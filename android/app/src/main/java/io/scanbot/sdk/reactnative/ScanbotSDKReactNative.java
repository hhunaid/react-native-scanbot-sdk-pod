/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import net.doo.snap.lib.detector.DetectionResult;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.scanbot.sdk.persistence.Page;
import io.scanbot.sdk.persistence.PageFileStorage;
import io.scanbot.sdk.process.ImageFilterType;

import static io.scanbot.sdk.reactnative.FileUtils.uriWithHash;

public class ScanbotSDKReactNative extends ReactContextBaseJavaModule {

    private final ExecutorService threadPool;

    public ScanbotSDKReactNative(final ReactApplicationContext reactContext) {
        super(reactContext);

        this.threadPool = Executors.newCachedThreadPool();
    }

    private ExecutorService getThreadPool() {
        return this.threadPool;
    }

    private static String getOptionValue(final ReadableMap options, final String optionName, final String defaultValue) {
        return options.hasKey(optionName) ? options.getString(optionName) : defaultValue;
    }

    private static boolean getOptionValue(final ReadableMap options, final String optionName, final boolean defaultValue) {
        return options.hasKey(optionName) ? options.getBoolean(optionName) : defaultValue;
    }

    private static int getOptionValue(final ReadableMap options, final String optionName, final int defaultValue) {
        return options.hasKey(optionName) ? options.getInt(optionName) : defaultValue;
    }

    private static double getOptionValue(final ReadableMap options, final String optionName, final double defaultValue) {
        return options.hasKey(optionName) ? options.getDouble(optionName) : defaultValue;
    }

    private static ReadableArray getOptionValue(final ReadableMap options, final String optionName) {
        return options.hasKey(optionName) ? options.getArray(optionName) : null;
    }

    private static boolean checkRejectSDKInitialization(final Promise promise) {
        if (!ScanbotSDKHelper.isSdkInitialized()) {
            ResponseUtils.errorMessageJson("Scanbot SDK is not initialized. Please call the initializeSDK() method first.", promise);
            return false;
        }
        return true;
    }

    private static void safeMap(ReadableMap options, Object target, Promise promise) {
        try {
            ObjectMapper.map(options, target);
        } catch (Exception ex) {
            promise.reject(ex);
        }
    }

    private static PageFileStorage getPageFileStorage() {
        return ScanbotSDKHelper.internalSDK.getPageFileStorage();
    }

    public static WritableMap convertNativePageToReact(Page nativePage) {
        WritableMap page = new WritableNativeMap();
        String pageId = nativePage.getPageId();
        page.putString("pageId", pageId);
        page.putString("detectionResult", JSONUtils.sdkDocDetectionResultToJsString(nativePage.getDetectionStatus()));
        page.putArray("polygon", JSONUtils.sdkPolygonToWritableArray(nativePage.getPolygon()));
        page.putString("filter", nativePage.getFilter().name());
        page.putString("originalImageFileUri", uriWithHash(getPageFileStorage().getImageURI(pageId, PageFileStorage.PageFileType.ORIGINAL)));
        page.putString("originalPreviewImageFileUri", uriWithHash(getPageFileStorage().getPreviewImageURI(pageId, PageFileStorage.PageFileType.ORIGINAL)));

        Uri documentImageUri = getPageFileStorage().getImageURI(pageId, PageFileStorage.PageFileType.DOCUMENT);
        if (new File(documentImageUri.getPath()).isFile()) {
            page.putString("documentImageFileUri", uriWithHash(documentImageUri));
            page.putString("documentPreviewImageFileUri", uriWithHash(getPageFileStorage().getPreviewImageURI(pageId, PageFileStorage.PageFileType.DOCUMENT)));
        }

        return page;
    }

    @Override
    public String getName() {
        return "ScanbotSDK";
    }

    @ReactMethod
    public void initializeSDK(final ReadableMap options, final Promise promise) {
        ScanbotSDKConfiguration config = new ScanbotSDKConfiguration();
        safeMap(options, config, promise);
        ScanbotSDKHelper.initializeSDK(config, getCurrentActivity().getApplication(), promise);
    }

    @ReactMethod
    public void isLicenseValid(final Promise promise) {
        if (!checkRejectSDKInitialization(promise)) { return; }
        promise.resolve(ScanbotSDKHelper.isLicenseActive());
    }

    @ReactMethod
    public void detectDocument(final String imageFileUri, final Promise promise) {
        this.getThreadPool().execute(new Runnable() {
            public void run() {
                if (!checkRejectSDKInitialization(promise)) { return; }
                ScanbotSDKHelper.detectDocument(imageFileUri, promise);
            }
        });
    }

    @ReactMethod
    public void applyImageFilter(final String imageFileUri, final String filterType, final Promise promise) {
        this.getThreadPool().execute(new Runnable() {
            public void run() {
                if (!checkRejectSDKInitialization(promise)) { return; }
                ScanbotSDKHelper.applyImageFilter(imageFileUri, filterType, promise);
            }
        });
    }

    @ReactMethod
    public void getOCRConfigs(final Promise promise) {
        if (!checkRejectSDKInitialization(promise)) { return; }
        ScanbotSDKHelper.getOcrConfigs(promise);
    }

    @ReactMethod
    public void performOCR(final ReadableArray imageFileUris, final ReadableArray languages, final ReadableMap options, final Promise promise) {
        this.getThreadPool().execute(new Runnable() {
            public void run() {
                if (!checkRejectSDKInitialization(promise)) { return; }

                final String outputFormat = getOptionValue(options, "outputFormat", "PLAIN_TEXT");

                ScanbotSDKHelper.performOCR(Arguments.toList(imageFileUris),
                    Arguments.toList(languages), outputFormat, promise);
            }
        });
    }

    @ReactMethod
    public void createPDF(final ReadableArray imageFileUris, final Promise promise) {
        this.getThreadPool().execute(new Runnable() {
            public void run() {
                if (!checkRejectSDKInitialization(promise)) { return; }
                ScanbotSDKHelper.createPDF(Arguments.toList(imageFileUris), promise);
            }
        });
    }

    @ReactMethod
    public void writeTIFF(final ReadableArray imageFileUris, final ReadableMap options, final Promise promise) {
        this.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                if (!checkRejectSDKInitialization(promise)) { return; }

                boolean binarized = getOptionValue(options, "oneBitEncoded", false);
                ScanbotSDKHelper.createTIFF(Arguments.toList(imageFileUris), binarized, promise);
            }
        });
    }

    @ReactMethod
    public void rotateImage(final String imageFileUri, final Double degrees, final Promise promise) {
        this.getThreadPool().execute(new Runnable() {
            public void run() {
                if (!checkRejectSDKInitialization(promise)) { return; }
                ScanbotSDKHelper.rotateImage(imageFileUri, degrees.floatValue(), promise);
            }
        });
    }

    @ReactMethod
    public void createPage(final String imageUri, final Promise promise) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!checkRejectSDKInitialization(promise)) { return; }
                    Bitmap bitmap = BitmapHelper.loadImage(imageUri, ScanbotSDKHelper.context);
                    String pageId = getPageFileStorage().add(bitmap);
                    Page page = new Page(pageId, Collections.<PointF>emptyList(), DetectionResult.OK);
                    promise.resolve(convertNativePageToReact(page));
                } catch (Exception ex) {
                    promise.reject(ex);
                }
            }
        });
    }

    @ReactMethod
    public void detectDocumentOnPage(final ReadableMap pageMap, final Promise promise) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!checkRejectSDKInitialization(promise)) { return; }
                    Page page = JSONUtils.convertReactToNativePage(pageMap);
                    page = ScanbotSDKHelper.internalSDK.pageProcessor().detectDocument(page);
                    promise.resolve(convertNativePageToReact(page));
                } catch (Exception ex) {
                    promise.reject(ex);
                }
            }
        });
    }

    @ReactMethod
    public void applyImageFilterOnPage(final ReadableMap pageMap, final String filterType, final Promise promise) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!checkRejectSDKInitialization(promise)) { return; }
                    Page page = JSONUtils.convertReactToNativePage(pageMap);
                    page = ScanbotSDKHelper.internalSDK.pageProcessor().applyFilter(page, BitmapHelper.imageFilterTypeFromString(filterType));
                    promise.resolve(convertNativePageToReact(page));
                } catch (Exception ex) {
                    promise.reject(ex);
                }
            }
        });
    }

    @ReactMethod
    public void rotatePage(final ReadableMap pageMap, final int times, final Promise promise) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!checkRejectSDKInitialization(promise)) { return; }
                    Page page = JSONUtils.convertReactToNativePage(pageMap);
                    ScanbotSDKHelper.internalSDK.pageProcessor().rotate(page, times);
                    promise.resolve(convertNativePageToReact(page));
                } catch (Exception ex) {
                    promise.reject(ex);
                }
            }
        });
    }

    @ReactMethod
    public void getFilteredDocumentPreviewUri(final ReadableMap pageMap, final String filterName, final Promise promise) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!checkRejectSDKInitialization(promise)) { return; }

                    Page page = JSONUtils.convertReactToNativePage(pageMap);
                    ImageFilterType filter = BitmapHelper.imageFilterTypeFromString(filterName);
                    PageFileStorage storage = ScanbotSDKHelper.internalSDK.pageFileStorage();
                    Uri uri = storage.getFilteredPreviewImageURI(page.getPageId(), filter);
                    if (!new File(uri.getPath()).exists()) {
                        ScanbotSDKHelper.internalSDK.pageProcessor().generateFilteredPreview(page, filter);
                    }

                    promise.resolve(uriWithHash(uri));
                } catch (Exception ex) {
                    promise.reject(ex);
                }
            }
        });
    }

    /*
     * ! getStoredPages() method dropped, due to potential abuse or wrong usage !
     * It provides page objects without meta data (polygon, filter, etc),
     * which may cause inconsistency (especially in our iOS SDK, e.g. rotatePage() method)!
    @ReactMethod
    public void getStoredPages(final Promise promise) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (!checkRejectSDKInitialization(promise)) { return; }
                try {
                    List<String> pageIds = getPageFileStorage().getStoredPages();
                    WritableArray pages = new WritableNativeArray();
                    for (String pageId : pageIds) {
                        pages.pushMap(convertNativePageToReact(new Page(pageId, Collections.<PointF>emptyList(), DetectionResult.OK)));
                    }
                    promise.resolve(pages);
                } catch (Exception ex) {
                    promise.reject(ex);
                }
            }
        });
    }
    */

    @ReactMethod
    public void removePage(final ReadableMap pageMap, final Promise promise) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (!checkRejectSDKInitialization(promise)) { return; }
                try {
                    String pageId = pageMap.getString("pageId");
                    boolean success = getPageFileStorage().remove(pageId);
                    if (!success) {
                        Log.w("ScanbotSDK", String.format("Deleting page %s failed", pageId));
                    }
                    promise.resolve(null);
                } catch (Exception ex) {
                    promise.reject(ex);
                }
            }
        });
    }

    @ReactMethod
    public void setDocumentImage(final ReadableMap pageMap, final String imageUri, final Promise promise) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (!checkRejectSDKInitialization(promise)) { return; }
                try {
                    Page page = JSONUtils.convertReactToNativePage(pageMap);
                    Bitmap bitmap = BitmapHelper.loadImage(imageUri, ScanbotSDKHelper.context);
                    getPageFileStorage().setImageForId(bitmap, page.getPageId(), PageFileStorage.PageFileType.DOCUMENT);
                    promise.resolve(convertNativePageToReact(page));
                } catch (Exception ex) {
                    promise.reject(ex);
                }
            }
        });
    }

    @ReactMethod
    public void cleanup(final Promise promise) {
        if (!checkRejectSDKInitialization(promise)) { return; }
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                getPageFileStorage().removeAll();

                // also resolves
                ScanbotSDKHelper.cleanup(promise);
            }
        });
    }

    @ReactMethod
    public void recognizeMrz(final String imageFileUri, final Promise promise) {
        this.getThreadPool().execute(new Runnable() {
            public void run() {
                if (!checkRejectSDKInitialization(promise)) {
                    return;
                }
                ScanbotSDKHelper.recognizeMrz(imageFileUri, promise);
            }
        });
    }
}
