/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;

import net.doo.snap.lib.detector.ContourDetector;
import net.doo.snap.util.FileChooserUtils;
import net.doo.snap.util.bitmap.BitmapUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.scanbot.sdk.process.ImageFilterType;

public class BitmapHelper {

    private static final Map<String, Integer> imageFilterMapping = new HashMap<String, Integer>();

    private static final Map<String, ImageFilterType> imageFilterTypeMapping = new HashMap<>();

    static {
        imageFilterMapping.put("NONE", ContourDetector.IMAGE_FILTER_NONE);
        imageFilterMapping.put("COLOR_ENHANCED", ContourDetector.IMAGE_FILTER_COLOR_ENHANCED);
        imageFilterMapping.put("GRAYSCALE", ContourDetector.IMAGE_FILTER_GRAY);
        imageFilterMapping.put("BINARIZED", ContourDetector.IMAGE_FILTER_BINARIZED);
        imageFilterMapping.put("COLOR_DOCUMENT", ContourDetector.IMAGE_FILTER_COLOR_DOCUMENT);
        imageFilterMapping.put("PURE_BINARIZED", ContourDetector.IMAGE_FILTER_PURE_BINARIZED);
        imageFilterMapping.put("BACKGROUND_CLEAN", ContourDetector.IMAGE_FILTER_BACKGROUND_CLEAN);
        imageFilterMapping.put("BLACK_AND_WHITE", ContourDetector.IMAGE_FILTER_BLACK_AND_WHITE);

        imageFilterTypeMapping.put("NONE", ImageFilterType.NONE);
        imageFilterTypeMapping.put("BINARIZED", ImageFilterType.BINARIZED);
        imageFilterTypeMapping.put("COLOR_DOCUMENT", ImageFilterType.COLOR_DOCUMENT);
        imageFilterTypeMapping.put("COLOR_ENHANCED", ImageFilterType.COLOR_ENHANCED);
        imageFilterTypeMapping.put("GRAYSCALE", ImageFilterType.GRAYSCALE);
        imageFilterTypeMapping.put("PURE_BINARIZED", ImageFilterType.PURE_BINARIZED);
        imageFilterTypeMapping.put("BACKGROUND_CLEAN", ImageFilterType.BACKGROUND_CLEAN);
        imageFilterTypeMapping.put("BLACK_AND_WHITE", ImageFilterType.BLACK_AND_WHITE);
    }

    public static Bitmap loadBitmap(final String imageFilePath) throws IOException {
        final Bitmap bitmap = BitmapUtils.decodeQuietly(imageFilePath, null);
        if (bitmap == null) {
            throw new IOException("Could not load image: " + imageFilePath);
        }
        return bitmap;
    }

    public static Bitmap loadImage(final String imageFile, final Context context) throws IOException {
        if (imageFile != null && !"".equals(imageFile.trim())) {
            if (imageFile.contains("://")) {
                final Uri uri = Uri.parse(imageFile);
                final String uriString = FileChooserUtils.getPath(context, uri);
                return loadBitmap(uriString);
            }
            return loadBitmap(imageFile);
        }
        throw new IllegalArgumentException("Invalid imageFile. Must be a file URI or a file path: " + imageFile);
    }

    public static Bitmap loadImage(final Uri imageUri, final Context context) throws IOException {
        return loadImage(FileChooserUtils.getPath(context, imageUri), context);
    }

    public static File storeImageAsFile(final Bitmap image, final int quality, final Context context) throws IOException {
        final File pictureFile = FileUtils.generateRandomTempScanbotFile("jpg", context);
        final FileOutputStream fos = new FileOutputStream(pictureFile);
        image.compress(Bitmap.CompressFormat.JPEG, quality, fos);
        fos.close();
        return pictureFile;
    }

    public static Uri storeImage(final Bitmap image, final int quality, final Context context) throws IOException {
        return Uri.fromFile(storeImageAsFile(image, quality, context));
    }

    public static Bitmap applyImageFilter(final Bitmap bitmap, final int imageFilter) {
        final ContourDetector detector = new ContourDetector();
        final List<PointF> polygon = new ArrayList<>();

        return detector.processImageAndRelease(bitmap, polygon, imageFilter);
    }

    public static int jsImageFilterToSdkFilter(final String imageFilter) {
        if (imageFilterMapping.containsKey(imageFilter)) {
            return imageFilterMapping.get(imageFilter);
        }
        throw new IllegalArgumentException("Unsupported imageFilter: " + imageFilter);
    }

    public static ImageFilterType imageFilterTypeFromString(String imageFilter) {
        if (imageFilterTypeMapping.containsKey(imageFilter)) {
            return imageFilterTypeMapping.get(imageFilter);
        }
        throw new IllegalArgumentException("Unsupported imageFilter: " + imageFilter);
    }

    public static Bitmap resizeImage(final Bitmap originalImage, final float width, final float height) {
        final float oldWidth = originalImage.getWidth();
        final float oldHeight = originalImage.getHeight();

        final float scaleFactor;
        if (oldWidth > oldHeight) {
            scaleFactor = width / oldWidth;
        } else {
            scaleFactor = height / oldHeight;
        }

        final float newHeight = oldHeight * scaleFactor;
        final float newWidth = oldWidth * scaleFactor;

        return Bitmap.createScaledBitmap(originalImage, (int) newWidth, (int) newHeight, false);
    }

    public static Bitmap cropAndWarpImage(final Bitmap bitmap, final List<PointF> polygon, final boolean releaseBitmap) {
        final ContourDetector detector = new ContourDetector();
        if (releaseBitmap) {
            return detector.processImageAndRelease(bitmap, polygon, ContourDetector.IMAGE_FILTER_NONE);
        } else {
            return detector.processImageF(bitmap, polygon, ContourDetector.IMAGE_FILTER_NONE);
        }
    }

    public static Bitmap rotateBitmap(Bitmap source, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
