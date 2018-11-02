/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/
package io.scanbot.sdk.reactnative;

import android.app.Application;
import android.content.Context;

import net.doo.snap.blob.BlobFactory;
import net.doo.snap.blob.BlobManager;
import net.doo.snap.persistence.PageFactory;
import net.doo.snap.persistence.cleanup.Cleaner;
import net.doo.snap.process.DocumentProcessor;
import net.doo.snap.process.TextRecognition;
import net.doo.snap.process.draft.DocumentDraftExtractor;

import io.scanbot.sdk.ScanbotSDK;

class ScanbotSDKWrapper {
    public ScanbotSDK scanbotSDK;
    public PageFactory pageFactory;
    public DocumentProcessor documentProcessor;
    public DocumentDraftExtractor documentDraftExtractor;
    public Cleaner sdkCleaner;
    public BlobManager blobManager;
    public BlobFactory blobFactory;
    public TextRecognition textRecognition;

    ScanbotSDKWrapper(Application application) {
        this.scanbotSDK = new ScanbotSDK(application);
        this.pageFactory = scanbotSDK.pageFactory();
        this.documentProcessor = scanbotSDK.documentProcessor();
        this.documentDraftExtractor = scanbotSDK.documentDraftExtractor();
        this.sdkCleaner = scanbotSDK.cleaner();
        this.blobManager = scanbotSDK.blobManager();
        this.blobFactory = scanbotSDK.blobFactory();
        this.textRecognition = scanbotSDK.textRecognition();
    }
}
