/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import android.app.Activity;

import net.doo.snap.ScanbotSDK;
import net.doo.snap.blob.BlobFactory;
import net.doo.snap.blob.BlobManager;
import net.doo.snap.persistence.PageFactory;
import net.doo.snap.persistence.cleanup.Cleaner;
import net.doo.snap.process.DocumentProcessor;
import net.doo.snap.process.TextRecognition;
import net.doo.snap.process.draft.DocumentDraftExtractor;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ScanbotSDKWrapper {

    class SDKInfoPackage {
        public ScanbotSDK scanbotSDK;
        public PageFactory pageFactory;
        public DocumentProcessor documentProcessor;
        public DocumentDraftExtractor documentDraftExtractor;
        public Cleaner sdkCleaner;
        public BlobManager blobManager;
        public BlobFactory blobFactory;
        public TextRecognition textRecognition;

        public SDKInfoPackage(Activity activity) {
            this.scanbotSDK = new ScanbotSDK(activity);
            this.pageFactory = scanbotSDK.pageFactory();
            this.documentProcessor = scanbotSDK.documentProcessor();
            this.documentDraftExtractor = scanbotSDK.documentDraftExtractor();
            this.sdkCleaner = scanbotSDK.cleaner();
            this.blobManager = scanbotSDK.blobManager();
            this.blobFactory = scanbotSDK.blobFactory();
            this.textRecognition = scanbotSDK.textRecognition();
        }
    }

    public SDKInfoPackage pack;

    public ScanbotSDKWrapper(final Activity activity) {
        FutureTask<SDKInfoPackage> initSDKTask = new FutureTask<SDKInfoPackage>(new Callable<SDKInfoPackage>() {
            @Override
            public SDKInfoPackage call() throws Exception {
                SDKInfoPackage pack = new SDKInfoPackage(activity);
                return pack;
            }
        });
        activity.runOnUiThread(initSDKTask);
        try {
            this.pack = initSDKTask.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
