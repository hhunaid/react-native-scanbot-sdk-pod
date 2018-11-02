package io.scanbot.sdk.reactnative.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import java.util.ArrayList;

import io.scanbot.mrzscanner.model.MRZField;
import io.scanbot.mrzscanner.model.MRZRecognitionResult;
import io.scanbot.sdk.barcode.entity.BarcodeFormat;
import io.scanbot.sdk.barcode.entity.BarcodeScanningResult;
import io.scanbot.sdk.persistence.Page;
import io.scanbot.sdk.reactnative.JSONUtils;
import io.scanbot.sdk.reactnative.ObjectMapper;
import io.scanbot.sdk.ui.view.barcode.BarcodeScannerActivity;
import io.scanbot.sdk.ui.view.barcode.configuration.BarcodeScannerConfiguration;
import io.scanbot.sdk.ui.view.base.configuration.CameraOrientationMode;
import io.scanbot.sdk.ui.view.camera.DocumentScannerActivity;
import io.scanbot.sdk.ui.view.camera.configuration.DocumentScannerConfiguration;
import io.scanbot.sdk.ui.view.edit.CroppingActivity;
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration;
import io.scanbot.sdk.ui.view.mrz.MRZScannerActivity;
import io.scanbot.sdk.ui.view.mrz.configuration.MRZScannerConfiguration;

import static io.scanbot.sdk.reactnative.ScanbotSDKReactNative.convertNativePageToReact;

public class DefaultUiModule extends ReactContextBaseJavaModule {
    public DefaultUiModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "SBSDKDefaultUi";
    }

    @ReactMethod
    public void startDocumentScanner(ReadableMap configuration, final Promise promise) {
        final ReactContext context = getReactApplicationContext();

        ActivityEventListener listener = new BaseActivityEventListener() {
            @Override
            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                if (requestCode != DOCUMENT_SCANNER_REQUEST_ID) {
                    return;
                }
                context.removeActivityEventListener(this);

                WritableMap result = new WritableNativeMap();
                result.putString("status", resultCode == Activity.RESULT_OK ? "OK" : "CANCELED");

                if (data != null)
                {
                    Parcelable[] nativePages = data.getParcelableArrayExtra(DocumentScannerActivity.SNAPPED_PAGE_EXTRA);
                    if (nativePages != null)
                    {
                        WritableArray pages = new WritableNativeArray();
                        for (Parcelable parcelable : nativePages) {
                            Page nativePage = (Page) parcelable;
                            pages.pushMap(convertNativePageToReact(nativePage));
                        }
                        result.putArray("pages", pages);
                    }
                }

                promise.resolve(result);
            }
        };

        try {
            DocumentScannerConfiguration nativeConfig = new DocumentScannerConfiguration();
            if (configuration.hasKey("orientationLockMode")) {
                String orientation = configuration.getString("orientationLockMode");
                switch (orientation) {
                    case "NONE": break;
                    case "PORTRAIT":
                        nativeConfig.setOrientationLockMode(CameraOrientationMode.PORTRAIT);
                        break;
                    case "PORTRAIT_UPSIDE_DOWN":
                        nativeConfig.setOrientationLockMode(CameraOrientationMode.PORTRAIT);
                        break;
                    case "LANDSCAPE_LEFT":
                        nativeConfig.setOrientationLockMode(CameraOrientationMode.LANDSCAPE);
                        break;
                    case "LANDSCAPE_RIGHT":
                        nativeConfig.setOrientationLockMode(CameraOrientationMode.LANDSCAPE);
                        break;
                    case "LANDSCAPE":
                        nativeConfig.setOrientationLockMode(CameraOrientationMode.LANDSCAPE);
                        break;
                    default:
                        promise.reject("error", String.format("Unsupported value specified for parameter orientationLockMode: %s", orientation));
                        return;
                }

                WritableMap updatedConfig = new WritableNativeMap();
                updatedConfig.merge(configuration);
                updatedConfig.putNull("orientationLockMode");
                configuration = updatedConfig;
            }

            ObjectMapper.map(configuration, nativeConfig);

            Intent startIntent = DocumentScannerActivity.newIntent(this.getCurrentActivity(), nativeConfig);

            context.addActivityEventListener(listener);
            context.startActivityForResult(startIntent, DOCUMENT_SCANNER_REQUEST_ID, null);
        } catch (Exception ex) {
            promise.reject(ex);
        }
    }

    @ReactMethod
    public void startCroppingScreen(ReadableMap page, ReadableMap configuration, final Promise promise) {
        final ReactContext context = getReactApplicationContext();

        ActivityEventListener listener = new BaseActivityEventListener() {
            @Override
            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                if (requestCode != CROPPING_SCREEN_REQUEST_ID) {
                    return;
                }
                context.removeActivityEventListener(this);

                WritableMap result = new WritableNativeMap();
                result.putString("status", resultCode == Activity.RESULT_OK ? "OK" : "CANCELED");

                if (data != null)
                {
                    Page nativePage = data.getParcelableExtra(CroppingActivity.EDITED_PAGE_EXTRA);
                    if (nativePage != null)
                    {
                        result.putMap("page", convertNativePageToReact(nativePage));
                    }
                }

                promise.resolve(result);
            }
        };

        try {
            CroppingConfiguration nativeConfig = new CroppingConfiguration();
            ObjectMapper.map(configuration, nativeConfig);
            nativeConfig.setPage(JSONUtils.convertReactToNativePage(page));
            Intent startIntent = CroppingActivity.newIntent(this.getCurrentActivity(), nativeConfig);

            context.addActivityEventListener(listener);
            context.startActivityForResult(startIntent, CROPPING_SCREEN_REQUEST_ID, null);
        } catch (Exception ex) {
            promise.reject(ex);
        }
    }

    @ReactMethod
    public void startMrzScanner(ReadableMap configuration, final Promise promise) {
        final ReactContext context = getReactApplicationContext();

        ActivityEventListener listener = new BaseActivityEventListener() {
            @Override
            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                if (requestCode != MRZ_SCANNER_REQUEST_ID) {
                    return;
                }
                context.removeActivityEventListener(this);

                WritableMap result = Arguments.createMap();

                if (resultCode == Activity.RESULT_OK && data != null)
                {
                    final MRZRecognitionResult mrzResult = data.getParcelableExtra(MRZScannerActivity.EXTRACTED_FIELDS_EXTRA);
                    if (mrzResult != null)
                    {
                        result = JSONUtils.sdkMRZRecognitionResultToWritableMap(mrzResult);
                    }
                }

                result.putString("status", resultCode == Activity.RESULT_OK ? "OK" : "CANCELED");
                promise.resolve(result);
            }
        };

        try {
            MRZScannerConfiguration nativeConfig = new MRZScannerConfiguration();
            ObjectMapper.map(configuration, nativeConfig);
            Intent startIntent = MRZScannerActivity.newIntent(this.getCurrentActivity(), nativeConfig);

            context.addActivityEventListener(listener);
            context.startActivityForResult(startIntent, MRZ_SCANNER_REQUEST_ID, null);
        } catch (Exception ex) {
            promise.reject(ex);
        }
    }


    @ReactMethod
    public void startBarcodeScanner(ReadableMap configuration, final Promise promise) {
        final ReactContext context = getReactApplicationContext();

        ActivityEventListener listener = new BaseActivityEventListener() {
            @Override
            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                if (requestCode != BARCODE_SCANNER_REQUEST_ID) {
                    return;
                }
                context.removeActivityEventListener(this);

                WritableMap result = new WritableNativeMap();
                result.putString("status", resultCode == Activity.RESULT_OK ? "OK" : "CANCELED");

                if (data != null)
                {
                    BarcodeScanningResult barcode = data.getParcelableExtra(BarcodeScannerActivity.SCANNED_BARCODE_EXTRA);
                    if (barcode != null)
                    {
                        result.putString("value", barcode.getText());
                        result.putString("format", barcode.getBarcodeFormat().name());
                    }
                }

                promise.resolve(result);
            }
        };

        try {
            BarcodeScannerConfiguration nativeConfig = new BarcodeScannerConfiguration();
            ObjectMapper.map(configuration, nativeConfig);

            if (configuration.hasKey("barcodeFormats")) {
                ArrayList barcodeFormats = configuration.getArray("barcodeFormats").toArrayList();
                ArrayList<BarcodeFormat> nativeBarcodeFormats = new ArrayList<>();
                for (Object format : barcodeFormats) {
                    nativeBarcodeFormats.add(BarcodeFormat.valueOf((String) format));
                }
                nativeConfig.setBarcodeFormatsFilter(nativeBarcodeFormats);
            }

            Intent startIntent = BarcodeScannerActivity.newIntent(this.getCurrentActivity(), nativeConfig);

            context.addActivityEventListener(listener);
            context.startActivityForResult(startIntent, BARCODE_SCANNER_REQUEST_ID, null);
        } catch (Exception ex) {
            promise.reject(ex);
        }
    }

    private final int DOCUMENT_SCANNER_REQUEST_ID = 31885;
    private final int CROPPING_SCREEN_REQUEST_ID = 31886;
    private final int MRZ_SCANNER_REQUEST_ID = 31887;
    private final int BARCODE_SCANNER_REQUEST_ID = 31888;
}
