/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class FileUtils {

    private static final String LOG_TAG = FileUtils.class.getSimpleName();


    private FileUtils() {
    }


    public static File getTempScanbotDirectory(final Context context) throws IOException {
        final File cacheDir;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // SD Card Mounted
            cacheDir = context.getExternalCacheDir();
        } else {
            // Use internal storage
            cacheDir = context.getCacheDir();
        }

        final File scanbotTempDir = new File(cacheDir, "sbsdk-temp");
        scanbotTempDir.mkdirs();
        // create it and make sure it exists
        if (!scanbotTempDir.isDirectory()) {
            throw new IOException("Can't create/get temporary cache directory: " + scanbotTempDir.getAbsolutePath());
        }
        return scanbotTempDir;
    }


    public static File generateRandomTempScanbotFile(final String extension, final Context context) throws IOException {
        final File tempDir = getTempScanbotDirectory(context);
        //final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //final String imageFileName = "scanbot_" + timeStamp + "." + extension;
        final String imageFileName = UUID.randomUUID().toString() + "." + extension;
        return new File(tempDir, imageFileName);
    }


    public static void cleanUpTempScanbotDirectory(final Activity activity) throws IOException {
        final File tempDir = getTempScanbotDirectory(activity);
        cleanDirectory(tempDir);
    }


    public static File getExternalStorageDirectory(final String directoryName) throws IOException {
        final File externalFilesDir = Environment.getExternalStorageDirectory();
        if (externalFilesDir == null) {
            throw new IOException("Can't get external storage directory");
        }

        final File result = new File(externalFilesDir, directoryName);
        if (!result.exists() && !result.mkdir()) {
            throw new IOException("Can't create sub folder in external storage directory");
        }

        return result;
    }

//    public static String pathForAsset(String asset, Context context) {
//        return "file:///android_asset/"+asset;
//        Uri path = Uri.fromFile(new File("//android_asset/"+asset));//Uri.parse("file:///android_asset/" + asset);
//        return path.toString();
//    }

    public static List<String> listAssetsInDir(String assetDirPath, Context context) {
        final List<String> result = new ArrayList<>();
        try {
            final String[] assets = context.getAssets().list(assetDirPath);

            if (assets != null && assets.length > 0) {
                for (final String asset : assets) {
                    result.add(asset);
                }
            }
        }
        catch (final IOException e) {
            /* catch IO exception since we want to return an empty list if assets dir does not exits. */
        }
        return result;
    }

    public static String pathCombine(String p1, String p2) {
        return new File(p1, p2).toString();
    }

    private static void copyStream(InputStream in, File dst) throws IOException {
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    public static void copyFile(File src, File dst) throws IOException {
        copyStream(new FileInputStream(src), dst);
    }

    public static void copyAsset(String assetName, File dst, Context context) throws IOException {
        final AssetManager am = context.getAssets();
        copyStream(am.open(assetName), dst);
    }

    public static void copyAssetFileIfNeeded(String assetFile, String targetFilePath, Context context) throws IOException {
        final File targetFile = new File(targetFilePath);
        if (!targetFile.exists()) {
            copyAsset(assetFile, targetFile, context);
        }
    }

    private static void cleanDirectory(final File directory) throws IOException {
        final File[] files = verifiedListFiles(directory);

        IOException exception = null;
        for (final File file : files) {
            try {
                forceDelete(file);
            } catch (final IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    private static File[] verifiedListFiles(File directory) throws IOException {
        if (!directory.exists()) {
            final String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            final String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        final File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }
        return files;
    }

    private static void forceDelete(final File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            final boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                throw new IOException("Could not delete file: " + file);
            }
        }
    }

    private static void deleteDirectory(final File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        cleanDirectory(directory);

        if (!directory.delete()) {
            throw new IOException("Could not delete directory: " + directory);
        }
    }
}
