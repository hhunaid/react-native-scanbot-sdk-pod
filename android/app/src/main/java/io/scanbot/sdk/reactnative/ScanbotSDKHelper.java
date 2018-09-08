/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;

import net.doo.snap.ScanbotSDK;
import net.doo.snap.ScanbotSDKInitializer;
import net.doo.snap.entity.Blob;
import net.doo.snap.entity.Document;
import net.doo.snap.entity.Language;
import net.doo.snap.entity.OcrStatus;
import net.doo.snap.entity.Page;
import net.doo.snap.entity.SnappingDraft;
import net.doo.snap.lib.detector.ContourDetector;
import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.persistence.DocumentStoreStrategy;
import net.doo.snap.process.DocumentProcessingResult;
import net.doo.snap.process.OcrResult;
import net.doo.snap.process.TextRecognition;
import net.doo.snap.process.util.DocumentDraft;
import net.doo.snap.util.FileChooserUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ScanbotSDKHelper {

    private static boolean isSdkInitialized = false;

    private final static String OCR_ASSETS_DIR_NAME = "SBSDKLanguageData";

    private static synchronized void setSdkInitialized(final boolean flag) {
        isSdkInitialized = flag;
    }

    public static synchronized boolean isSdkInitialized() {
        return isSdkInitialized;
    }

    public static void initializeSDK(final String licenseKey, final boolean loggingEnabled,
                                     final Activity activity, final Callback successCallback,
                                     final Callback errorCallback) {
        if (isSdkInitialized()) {
            ResponseUtils.successMessageJson("Scanbot SDK has already been initialized.", successCallback);
            return;
        }

        try {
            final String callbackMessage;
            final ScanbotSDKInitializer initializer = new ScanbotSDKInitializer();
            initializer.withLogging(loggingEnabled);
            if (licenseKey != null && !"".equals(licenseKey.trim()) && !"null".equals(licenseKey.toLowerCase())) {
                initializer.license(activity.getApplication(), licenseKey);
                callbackMessage = "Scanbot SDK initialized.";
            }
            else {
                callbackMessage = "Trial mode activated. You can now test all features for 60 seconds.";
            }

            // To be consistent with iOS SDK set OCR paths to "SBSDKLanguageData" which should be provided via assets.
            initializer.ocrBlobsPath(activity.getApplication(), OCR_ASSETS_DIR_NAME);
            initializer.languageClassifierBlobPath(activity.getApplication(), OCR_ASSETS_DIR_NAME);

            initializer.initialize(activity.getApplication());
            prepareOcrBlobs(successCallback, errorCallback, callbackMessage, activity);
        }
        catch (final Exception e) {
            final String errorMsg = "Error initializing Scanbot SDK: " + e.getMessage();
            ResponseUtils.errorMessageJson(errorMsg, errorCallback);
        }
    }

    private static void prepareDefaultOcrBlobs(final ScanbotSDKWrapper wrapper, final Activity activity) throws IOException {
        // Copy languageClassifier2.bin from assets to SDK internal OCR blobs directory
        for (final Blob blob : wrapper.pack.blobFactory.languageDetectorBlobs()) {
            if (!wrapper.pack.blobManager.isBlobAvailable(blob)) {
                wrapper.pack.blobManager.fetch(blob, false);
            }
        }

        // Copy pdf.ttf from assets to SDK internal OCR blobs directory
        final Blob pdfBlob = new Blob(new File(wrapper.pack.blobManager.getOCRBlobsDirectory(), "pdf.ttf").getPath(), "pdf.ttf");
        if (!wrapper.pack.blobManager.isBlobAvailable(pdfBlob)) {
            wrapper.pack.blobManager.fetch(pdfBlob, false);
        }

        // Check and copy all available language blob files from assets to SDK internal OCR blobs directory:
        final String blobsDir = wrapper.pack.blobManager.getOCRBlobsDirectory().getAbsolutePath();
        for(final String assetFile : FileUtils.listAssetsInDir(OCR_ASSETS_DIR_NAME, activity)) {
            if (assetFile.endsWith(".traineddata")) {
                final String source = OCR_ASSETS_DIR_NAME + File.separator + assetFile;
                final String targetFilePath = blobsDir + File.separator + assetFile;
                FileUtils.copyAssetFileIfNeeded(source, targetFilePath, activity);
            }
        }
    }

    private static void prepareOcrBlobs(final Callback successCallback, final Callback errorCallback,
                                        final String callbackMessage, final Activity activity) {
        final ScanbotSDKWrapper wrapper = new ScanbotSDKWrapper(activity);
        try {
            prepareDefaultOcrBlobs(wrapper, activity);
            setSdkInitialized(true);
            ResponseUtils.successMessageJson(callbackMessage, successCallback);
        }
        catch (final Exception e) {
            final String errorMsg = "Error initializing Scanbot SDK: " + e.getMessage();
            ResponseUtils.errorMessageJson(errorMsg, errorCallback);
        }
    }

    public static boolean isLicenseActive(final ScanbotSDK scanbotSDK) {
        return scanbotSDK != null && scanbotSDK.isLicenseActive();
    }

    public static DocumentDetectionResult detectDocument(final Bitmap bitmap, final boolean releaseBitmap) {
        final ContourDetector detector = new ContourDetector();
        final DetectionResult sdkDetectionResult = detector.detect(bitmap);
        final List<PointF> polygon = detector.getPolygonF();
        final Bitmap documentImage;
        if (releaseBitmap) {
            documentImage = detector.processImageAndRelease(bitmap, polygon, ContourDetector.IMAGE_FILTER_NONE);
        } else {
            documentImage = detector.processImageF(bitmap, polygon, ContourDetector.IMAGE_FILTER_NONE);
        }

        return new DocumentDetectionResult(sdkDetectionResult, polygon, documentImage);
    }

    public static void documentDetection(final String imageFileUri, final int quality,
                                         final Context context, final Callback successCallback,
                                         final Callback errorCallback) throws IOException {
        try {
            final Bitmap sourceImage = BitmapHelper.loadImage(imageFileUri, context);
            final DocumentDetectionResult result = detectDocument(sourceImage, true);

            Uri resultImgUri = null;
            if (result.documentImage != null) {
                resultImgUri = BitmapHelper.storeImage(result.documentImage, quality, context);
            }

            final WritableMap response = Arguments.createMap();
            response.putString("detectionResult", JSONUtils.sdkDocDetectionResultToJsString(result.sdkDetectionResult));
            response.putString("imageFileUri", (resultImgUri != null ? resultImgUri.toString() : ""));
            response.putArray("polygon", JSONUtils.sdkPolygonToWritableArray(result.polygon));
            successCallback.invoke(response);
        }
        catch (final Exception e) {
            final String errorMsg = "Could not perform document detection on image: " + e.getMessage();
            ResponseUtils.errorMessageJson(errorMsg, errorCallback);
        }
    }

    public static void applyImageFilter(final String imageFileUri, final String imageFilter,
                                        final int quality, final Activity activity,
                                        final Callback successCallback, final Callback errorCallback) throws IOException {
        try {
            final Bitmap bitmap = BitmapHelper.loadImage(imageFileUri, activity);
            final Bitmap result = BitmapHelper.applyImageFilter(bitmap, BitmapHelper.jsImageFilterToSdkFilter(imageFilter));
            final Uri resultImgUri = BitmapHelper.storeImage(result, quality, activity);

            final WritableMap response = Arguments.createMap();
            response.putString("imageFileUri", resultImgUri.toString());
            successCallback.invoke(response);
        }
        catch (final Exception e) {
            final String errorMsg = "Error applying filter on image: " + e.getMessage();
            ResponseUtils.errorMessageJson(errorMsg, errorCallback);
        }
    }

    public static List<String> getInstalledOcrLanguages(final ScanbotSDKWrapper wrapper) throws IOException {
        final List<String> installedLanguages = new ArrayList<>();
        final Set<Language> allLanguagesWithAvailableOcrBlobs = wrapper.pack.blobManager.getAllLanguagesWithAvailableOcrBlobs();
        for (final Language language : allLanguagesWithAvailableOcrBlobs) {
            installedLanguages.add(language.getIso1Code());
        }
        return installedLanguages;
    }

    public static void getOcrConfigs(final Activity activity, final Callback successCallback,
                                     final Callback errorCallback) throws IOException {
        final ScanbotSDKWrapper wrapper = new ScanbotSDKWrapper(activity);

        try {
            final List<String> languages = getInstalledOcrLanguages(wrapper);
            final File ocrBlobsDir = wrapper.pack.blobManager.getOCRBlobsDirectory();
            final WritableMap response = Arguments.createMap();
            response.putString("languageDataPath", Uri.fromFile(ocrBlobsDir).toString());
            response.putArray("installedLanguages", ResponseUtils.writableStringArray(languages));
            successCallback.invoke(response);
        } catch (final Exception e) {
            final String errorMsg = "Could not get OCR configs: " + e.getMessage();
            ResponseUtils.errorMessageJson(errorMsg, errorCallback);
        }
    }

    private static OcrResult getOCRResult(final List<String> languagesIsoCodes,
                                          TextRecognition textRecognition,
                                          final List<Uri> images,
                                          String outputFormat,
                                          final Activity activity) throws IOException {
        final ScanbotSDKWrapper wrapper = new ScanbotSDKWrapper(activity);

        List<Language> languages = new ArrayList<>();
        for (String languageIsoCode : languagesIsoCodes) {
            languages.add(Language.languageByIso(languageIsoCode));
        }
        for (Language language : languages) {
            Collection<Blob> ocrBlobs = wrapper.pack.blobFactory.ocrLanguageBlobs(language);
            for (Blob blob : ocrBlobs) {
                if (!wrapper.pack.blobManager.isBlobAvailable(blob)) {
                    throw new IOException("OCR blobs for selected languages were not found.");
                }
            }
        }

        List<Page> pages = new ArrayList<>();
        for (Uri image : images) {
            pages.add(wrapper.pack.pageFactory.buildPage(new File(image.getPath())));
        }

        if (outputFormat.equals("PLAIN_TEXT")) {
            return textRecognition
                    .withoutPDF(
                            languages,
                            pages
                    ).recognize();
        } else {
            Document document = new Document();
            document.setName("document.pdf");
            document.setOcrStatus(OcrStatus.PENDING);
            document.setId("id");
            return textRecognition
                    .withPDF(
                            languages,
                            document,
                            pages
                    ).recognize();
        }
    }

    private static List<Uri> uriListFromStringList(final List<String> strings) {
        final List<Uri> result = new ArrayList<>();
        for (final String uri : strings) {
            result.add(Uri.parse(uri));
        }
        return result;
    }

    public static void performOCR(final List<String> images, final List<String> languages,
                                  final String outputFormat, final Activity activity,
                                  final Callback successCallback, final Callback errorCallback) throws IOException {
        final ScanbotSDKWrapper wrapper = new ScanbotSDKWrapper(activity);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        final DocumentStoreStrategy documentStoreStrategy = new DocumentStoreStrategy(activity, preferences);
        final TextRecognition textRecognition = wrapper.pack.textRecognition;

        File tempPdfFile = null;
        try {
            final List<String> check = new ArrayList<>(languages);
            check.removeAll(getInstalledOcrLanguages(wrapper));
            if (!check.isEmpty()) {
                final String errorMsg = "Missing OCR language files for languages: " + check.toString();
                ResponseUtils.errorMessageJson(errorMsg, errorCallback);
                return;
            }

            final OcrResult result = getOCRResult(languages, textRecognition, uriListFromStringList(images), outputFormat, activity);

            final WritableMap response = Arguments.createMap();
            if (outputFormat.equals("PLAIN_TEXT")) {
                response.putString("plainText", result.recognizedText);
            }
            else if (outputFormat.equals("PDF_FILE") || outputFormat.equals("FULL_OCR_RESULT")) {
                tempPdfFile = documentStoreStrategy.getDocumentFile(result.sandwichedPdfDocument.getId(), result.sandwichedPdfDocument.getName());
                final Uri pdfOutputUri = Uri.fromFile(FileUtils.generateRandomTempScanbotFile("pdf", activity));
                FileUtils.copyFile(tempPdfFile, new File(pdfOutputUri.getPath()));
                response.putString("pdfFileUri", pdfOutputUri.toString());
                if (outputFormat.equals("FULL_OCR_RESULT")) {
                    response.putString("plainText", result.recognizedText);
                }
            }
            else {
                response.putString("plainText", result.recognizedText);
            }
            successCallback.invoke(response);
        }
        catch (final Exception e) {
            final String errorMsg = "Could not perform OCR on images: " + e.getMessage();
            ResponseUtils.errorMessageJson(errorMsg, errorCallback);
        }
        finally {
            if (tempPdfFile != null && tempPdfFile.exists()) {
                tempPdfFile.delete();
            }
        }
    }

    public static void createPDF(final List<String> images, final Activity activity,
                                 final Callback successCallback, final Callback errorCallback) throws IOException {
        final ScanbotSDKWrapper wrapper = new ScanbotSDKWrapper(activity);
        File tempPdfFile = null;
        try {
            final SnappingDraft snappingDraft = new SnappingDraft();
            snappingDraft.setDocumentName(UUID.randomUUID().toString());

            for (final Uri imageUri : uriListFromStringList(images)) {
                final String path = FileChooserUtils.getPath(activity, imageUri);
                final File file = new File(path);
                final Page page = wrapper.pack.pageFactory.buildPage(file);
                snappingDraft.addPage(page);
            }

            final DocumentDraft[] documentDrafts = wrapper.pack.documentDraftExtractor.extract(snappingDraft);
            final DocumentDraft draft = documentDrafts[0];
            final DocumentProcessingResult result = wrapper.pack.documentProcessor.processDocument(draft);

            tempPdfFile = result.getDocumentFile();

            final Uri pdfOutputUri = Uri.fromFile(FileUtils.generateRandomTempScanbotFile("pdf", activity));
            FileUtils.copyFile(tempPdfFile, new File(pdfOutputUri.getPath()));

            final WritableMap response = Arguments.createMap();
            response.putString("pdfFileUri", pdfOutputUri.toString());
            successCallback.invoke(response);
        }
        catch (final Exception e) {
            ResponseUtils.errorMessageJson("Error creating PDF: " + e.getMessage(), errorCallback);
        }
        finally {
            if (tempPdfFile != null && tempPdfFile.exists()) {
                tempPdfFile.delete();
            }
            wrapper.pack.sdkCleaner.cleanUp();
        }
    }

    public static void rotateImage(final String imageFileUri, int degrees, int compressionQuality,
                                   final Activity activity, final Callback successCallback,
                                   final Callback errorCallback) throws IOException {
        try {
            final Bitmap bitmap = BitmapHelper.loadImage(imageFileUri, activity);
            final Uri resultImgUri = BitmapHelper.storeImage(BitmapHelper.rotateBitmap(bitmap, -degrees), compressionQuality, activity);
            final WritableMap response = Arguments.createMap();
            response.putString("imageFileUri", resultImgUri.toString());
            successCallback.invoke(response);
        }
        catch (final Exception e) {
            ResponseUtils.errorMessageJson("Error applying rotation on image: " + e.getMessage(), errorCallback);
        }
    }

    public static void cleanup(final Activity activity, final Callback successCallback,
                               final Callback errorCallback) throws IOException {
        try {
            FileUtils.cleanUpTempScanbotDirectory(activity);
            ResponseUtils.successMessageJson("Cleanup successfully done", successCallback);
        }
        catch (final Exception e) {
            ResponseUtils.errorMessageJson("Could not cleanup the temporary " +
                    "directory of Scanbot SDK Plugin: " + e.getMessage(), errorCallback);
        }
    }

}
