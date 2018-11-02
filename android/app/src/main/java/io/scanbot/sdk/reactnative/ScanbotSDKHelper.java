/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;

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

import io.scanbot.mrzscanner.model.MRZField;
import io.scanbot.mrzscanner.model.MRZRecognitionResult;
import io.scanbot.sdk.ScanbotSDK;
import io.scanbot.sdk.ScanbotSDKInitializer;
import io.scanbot.sdk.persistence.PageStorageSettings;
import io.scanbot.tiffwriter.TIFFWriter;

public class ScanbotSDKHelper {

    private static boolean isSdkInitialized = false;

    private final static String OCR_ASSETS_DIR_NAME = "SBSDKLanguageData";

    private static synchronized void setSdkInitialized(final boolean flag) {
        isSdkInitialized = flag;
    }

    private static ScanbotSDKWrapper wrapper;
    private static PageStorageSettings pageStorageSettings;

    public static Context context;
    public static ScanbotSDK internalSDK;

    public static synchronized boolean isSdkInitialized() {
        return isSdkInitialized;
    }

    public static void initializeSDK(final ScanbotSDKConfiguration config,
                                     final Application application, final Promise promise) {
        if (isSdkInitialized()) {
            ResponseUtils.successMessageJson("Scanbot SDK has already been initialized.", promise);
            return;
        }

        try {
            final String callbackMessage;
            final ScanbotSDKInitializer initializer = new ScanbotSDKInitializer();
            initializer.withLogging(config.isLoggingEnabled());
            String licenseKey = config.getLicenseKey();
            if (licenseKey != null && !"".equals(licenseKey.trim()) && !"null".equals(licenseKey.toLowerCase())) {
                initializer.license(application, licenseKey);
                callbackMessage = "Scanbot SDK initialized.";
            }
            else {
                callbackMessage = "Trial mode activated. You can now test all features for 60 seconds.";
            }

            // To be consistent with iOS SDK set OCR paths to "SBSDKLanguageData" which should be provided via assets.
            initializer.ocrBlobsPath(application, OCR_ASSETS_DIR_NAME);
            initializer.languageClassifierBlobPath(application, OCR_ASSETS_DIR_NAME);

            pageStorageSettings = new PageStorageSettings.Builder()
                    .imageFormat(config.getStorageImageFormat())
                    .imageQuality(config.getStorageImageQuality())
                    .build();

            initializer.usePageStorageSettings(pageStorageSettings);

            initializer.initialize(application);

            wrapper = new ScanbotSDKWrapper(application);
            ScanbotSDKHelper.context = application;
            internalSDK = wrapper.scanbotSDK;

            prepareDefaultOcrBlobs();

            setSdkInitialized(true);
            ResponseUtils.successMessageJson(callbackMessage, promise);
        }
        catch (final Exception e) {
            promise.reject(e);
        }
    }

    private static void prepareDefaultOcrBlobs() throws IOException {
        // Copy languageClassifier2.bin from assets to SDK internal OCR blobs directory
        for (final Blob blob : wrapper.blobFactory.languageDetectorBlobs()) {
            if (!wrapper.blobManager.isBlobAvailable(blob)) {
                wrapper.blobManager.fetch(blob, false);
            }
        }

        // Copy pdf.ttf from assets to SDK internal OCR blobs directory
        final Blob pdfBlob = new Blob(new File(wrapper.blobManager.getOCRBlobsDirectory(), "pdf.ttf").getPath(), "pdf.ttf");
        if (!wrapper.blobManager.isBlobAvailable(pdfBlob)) {
            wrapper.blobManager.fetch(pdfBlob, false);
        }

        // Check and copy all available language blob files from assets to SDK internal OCR blobs directory:
        final String blobsDir = wrapper.blobManager.getOCRBlobsDirectory().getAbsolutePath();
        for(final String assetFile : FileUtils.listAssetsInDir(OCR_ASSETS_DIR_NAME, context)) {
            if (assetFile.endsWith(".traineddata")) {
                final String source = OCR_ASSETS_DIR_NAME + File.separator + assetFile;
                final String targetFilePath = blobsDir + File.separator + assetFile;
                FileUtils.copyAssetFileIfNeeded(source, targetFilePath, context);
            }
        }
    }

    public static boolean isLicenseActive() {
        return wrapper.scanbotSDK.isLicenseActive();
    }

    public static DocumentDetectionResult detectDocument(final String imageFileUri) throws IOException {
        final Bitmap bitmap = BitmapHelper.loadImage(imageFileUri, context);
        final ContourDetector detector = new ContourDetector();
        final DetectionResult sdkDetectionResult = detector.detect(bitmap);
        final List<PointF> polygon = detector.getPolygonF();
        final Bitmap documentImage = detector.processImageAndRelease(bitmap, polygon, ContourDetector.IMAGE_FILTER_NONE);
        return new DocumentDetectionResult(sdkDetectionResult, polygon, documentImage);
    }

    public static void detectDocument(final String imageFileUri, final Promise promise) {
        try {
            final DocumentDetectionResult result = detectDocument(imageFileUri);

            Uri resultImgUri = null;
            if (result.documentImage != null) {
                resultImgUri = BitmapHelper.storeImage(result.documentImage, pageStorageSettings.getImageQuality(), context);
            }

            final WritableMap response = Arguments.createMap();
            response.putString("detectionResult", JSONUtils.sdkDocDetectionResultToJsString(result.sdkDetectionResult));
            response.putString("documentImageFileUri", (resultImgUri != null ? resultImgUri.toString() : ""));
            response.putArray("polygon", JSONUtils.sdkPolygonToWritableArray(result.polygon));
            promise.resolve(response);
        }
        catch (final Exception e) {
            promise.reject(e);
        }
    }

    public static void applyImageFilter(final String imageFileUri, final String imageFilter, final Promise promise) {
        try {
            final Bitmap bitmap = BitmapHelper.loadImage(imageFileUri, context);
            final Bitmap result = BitmapHelper.applyImageFilter(bitmap, BitmapHelper.jsImageFilterToSdkFilter(imageFilter));
            final Uri resultImgUri = BitmapHelper.storeImage(result, pageStorageSettings.getImageQuality(), context);

            final WritableMap response = Arguments.createMap();
            response.putString("imageFileUri", resultImgUri.toString());
            promise.resolve(response);
        }
        catch (final Exception e) {
            promise.reject(e);
        }
    }

    private static List<String> getInstalledOcrLanguages() throws IOException {
        final List<String> installedLanguages = new ArrayList<>();
        final Set<Language> allLanguagesWithAvailableOcrBlobs = wrapper.blobManager.getAllLanguagesWithAvailableOcrBlobs();
        for (final Language language : allLanguagesWithAvailableOcrBlobs) {
            installedLanguages.add(language.getIso1Code());
        }
        return installedLanguages;
    }

    public static void getOcrConfigs(final Promise promise) {
        try {
            final List<String> languages = getInstalledOcrLanguages();
            final File ocrBlobsDir = wrapper.blobManager.getOCRBlobsDirectory();
            final WritableMap response = Arguments.createMap();
            response.putString("languageDataPath", Uri.fromFile(ocrBlobsDir).toString());
            response.putArray("installedLanguages", Arguments.fromList(languages));
            promise.resolve(response);
        } catch (final Exception e) {
            promise.reject(e);
        }
    }

    private static OcrResult getOCRResult(final List<String> languagesIsoCodes,
                                          TextRecognition textRecognition,
                                          final List<Uri> images,
                                          String outputFormat) throws IOException {
        try {
            List<Language> languages = new ArrayList<>();
            for (String languageIsoCode : languagesIsoCodes) {
                languages.add(Language.languageByIso(languageIsoCode));
            }
            for (Language language : languages) {
                Collection<Blob> ocrBlobs = wrapper.blobFactory.ocrLanguageBlobs(language);
                for (Blob blob : ocrBlobs) {
                    if (!wrapper.blobManager.isBlobAvailable(blob)) {
                        throw new IOException("OCR blobs for selected languages were not found.");
                    }
                }
            }

            List<Page> pages = new ArrayList<>();
            for (Uri image : images) {
                pages.add(wrapper.pageFactory.buildPage(new File(image.getPath())));
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
        } finally {
            internalSDK.cleaner().cleanUp();
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
                                  final String outputFormat,
                                  final Promise promise) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final DocumentStoreStrategy documentStoreStrategy = new DocumentStoreStrategy(context, preferences);
        final TextRecognition textRecognition = wrapper.textRecognition;

        File tempPdfFile = null;
        try {
            final List<String> check = new ArrayList<>(languages);
            check.removeAll(getInstalledOcrLanguages());
            if (!check.isEmpty()) {
                final String errorMsg = "Missing OCR language files for languages: " + check.toString();
                ResponseUtils.errorMessageJson(errorMsg, promise);
                return;
            }

            final OcrResult result = getOCRResult(languages, textRecognition, uriListFromStringList(images), outputFormat);

            final WritableMap response = Arguments.createMap();
            if (outputFormat.equals("PLAIN_TEXT")) {
                response.putString("plainText", result.recognizedText);
            }
            else if (outputFormat.equals("PDF_FILE") || outputFormat.equals("FULL_OCR_RESULT")) {
                tempPdfFile = documentStoreStrategy.getDocumentFile(result.sandwichedPdfDocument.getId(), result.sandwichedPdfDocument.getName());
                final Uri pdfOutputUri = Uri.fromFile(FileUtils.generateRandomTempScanbotFile("pdf", context));
                if (!tempPdfFile.renameTo(new File(pdfOutputUri.getPath()))) {
                    promise.reject("error", "Unable to write to destination path.");
                    return;
                }
                response.putString("pdfFileUri", pdfOutputUri.toString());
                if (outputFormat.equals("FULL_OCR_RESULT")) {
                    response.putString("plainText", result.recognizedText);
                }
            }
            else {
                response.putString("plainText", result.recognizedText);
            }
            promise.resolve(response);
        }
        catch (final Exception e) {
            promise.reject(e);
        }
    }

    public static void createPDF(final List<String> images, final Promise promise) {
        File tempPdfFile = null;
        try {
            final SnappingDraft snappingDraft = new SnappingDraft();
            snappingDraft.setDocumentName(UUID.randomUUID().toString());

            for (final Uri imageUri : uriListFromStringList(images)) {
                final String path = FileChooserUtils.getPath(context, imageUri);
                final File file = new File(path);
                final Page page = wrapper.pageFactory.buildPage(file);
                snappingDraft.addPage(page);
            }

            final DocumentDraft[] documentDrafts = wrapper.documentDraftExtractor.extract(snappingDraft);
            final DocumentDraft draft = documentDrafts[0];
            final DocumentProcessingResult result = wrapper.documentProcessor.processDocument(draft);

            tempPdfFile = result.getDocumentFile();

            final Uri pdfOutputUri = Uri.fromFile(FileUtils.generateRandomTempScanbotFile("pdf", context));
            if (!tempPdfFile.renameTo(new File(pdfOutputUri.getPath()))) {
                promise.reject("error", "Unable to write to destination path.");
                return;
            }

            final WritableMap response = Arguments.createMap();
            response.putString("pdfFileUri", pdfOutputUri.toString());
            promise.resolve(response);
        } catch (final Exception e) {
            promise.reject(e);
        } finally {
            internalSDK.cleaner().cleanUp();
        }
    }

    public static void createTIFF(final List<String> images, boolean binarized, final Promise promise) {
        try {
            TIFFWriter writer = new TIFFWriter();

            List<File> files = new ArrayList<>();
            for (String uri : images) {
                File file = new File(Uri.parse(uri).getPath());
                if (!file.exists()) {
                    promise.reject("error", String.format("File not found: %s", uri));
                    return;
                }
                files.add(file);
            }

            final File tiffOutputFile = FileUtils.generateRandomTempScanbotFile("tiff", context);
            boolean success = binarized
                    ? writer.writeBinarizedMultiPageTIFFFromFileList(files, tiffOutputFile)
                    : writer.writeMultiPageTIFFFromFileList(files, tiffOutputFile);

            if (success) {
                final WritableMap response = Arguments.createMap();
                response.putString("tiffFileUri", tiffOutputFile.getAbsolutePath());
                promise.resolve(response);
            } else {
                promise.reject("error", "Unable to write TIFF.");
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    public static void rotateImage(final String imageFileUri, float degrees, final Promise promise) {
        try {
            final Bitmap bitmap = BitmapHelper.loadImage(imageFileUri, context);
            final Uri resultImgUri = BitmapHelper.storeImage(BitmapHelper.rotateBitmap(bitmap, -degrees), pageStorageSettings.getImageQuality(), context);
            final WritableMap response = Arguments.createMap();
            response.putString("imageFileUri", resultImgUri.toString());
            promise.resolve(response);
        }
        catch (final Exception e) {
            promise.reject(e);
        }
    }

    public static void cleanup(final Promise promise) {
        try {
            FileUtils.cleanUpTempScanbotDirectory(context);
            wrapper.sdkCleaner.cleanUp();
            ResponseUtils.successMessageJson("Cleanup successfully done", promise);
        }
        catch (final Exception e) {
            promise.reject(e);
        }
    }

    public static void recognizeMrz(final String imageFileUri, final Promise promise) {
        try {
            final Bitmap image = BitmapHelper.loadImage(imageFileUri, context);

            final MRZRecognitionResult mrzRecognitionResult = internalSDK.mrzScanner().recognizeMRZBitmap(image, 0);
            final WritableMap result = JSONUtils.sdkMRZRecognitionResultToWritableMap(mrzRecognitionResult);
            promise.resolve(result);
        }
        catch (final Exception e) {
            promise.reject(e);
        }
    }

}
