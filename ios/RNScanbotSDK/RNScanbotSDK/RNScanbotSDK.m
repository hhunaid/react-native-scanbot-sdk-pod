//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import "RNScanbotSDK.h"
#import "React/RCTLog.h"

#import "SharedConfiguration.h"
#import "ImageUtils.h"
#import "OCRUtils.h"
#import "HashUtils.h"
#import "SBSDKPolygon+JSON.h"
#import "JSONOptionsUtils.h"
#import "JSONMappings.h"
#import "ObjectMapper.h"
#import "ScanbotSDKConfiguration.h"

@import ScanbotSDK;

static BOOL rejectIfUninitialized(RCTPromiseRejectBlock reject) {
    if (![SharedConfiguration isSDKInitialized]) {
        reject(@"error", @"ScanbotSDK is not initialized", nil);
        return TRUE;
    }
    return FALSE;
}

@implementation RNScanbotSDK

RCT_EXPORT_MODULE(ScanbotSDK);

#pragma mark - ReactNative exported interfaces

RCT_EXPORT_METHOD(initializeSDK:(NSDictionary *)options
				  success:(RCTPromiseResolveBlock)resolve
				  failure:(RCTPromiseRejectBlock)reject) {
    if ([SharedConfiguration isSDKInitialized]) {
        resolve(@{@"result": @"ScanbotSDK has already been initialized."});
        return;
    }
    
    [ObjectMapper setEnumerationMapping:@{
                                          @"orientationLockMode": @{
                                                  @"NONE": @(SBSDKOrientationLockNone),
                                                  @"PORTRAIT": @(SBSDKOrientationLockPortrait),
                                                  @"PORTRAIT_UPSIDE_DOWN": @(SBSDKOrientationLockPortraitUpsideDown),
                                                  @"LANDSCAPE_LEFT": @(SBSDKOrientationLockLandscapeLeft),
                                                  @"LANDSCAPE_RIGHT": @(SBSDKOrientationLockLandscapeRight),
                                                  @"LANDSCAPE": @(SBSDKOrientationLockLandscapeLeft), // TODO
                                                  },
                                          @"storageImageFormat": @{
                                                  @"JPG": @(SBSDKImageFileFormatJPEG),
                                                  @"PNG": @(SBSDKImageFileFormatPNG)
                                                  },
                                          @"cameraPreviewMode": @{
                                                  @"FILL_IN": @(SBSDKUIVideoContentModeFillIn),
                                                  @"FIT_IN": @(SBSDKUIVideoContentModeFitIn),
                                                  }
                                          }];

    ScanbotSDKConfiguration* config = [[ScanbotSDKConfiguration alloc] init];
    [ObjectMapper populateInstance:config fromDictionary:options];
    [SharedConfiguration defaultConfiguration].sdkConfiguration = config;

    [ScanbotSDK setSharedApplication:[UIApplication sharedApplication]];

    NSString *initMessage = @"Scanbot SDK initialized.";

    if (config.licenseKey && ![config.licenseKey isEqualToString:@""]) {
        if (![ScanbotSDK setLicense:config.licenseKey]) {
            reject(@"error", @"License key is invalid.", nil);
            return;
        }
    } else {
        initMessage = @"Trial mode activated. You can now test all features for 60 seconds.";
    }

    if (config.loggingEnabled) {
        [ScanbotSDK setLoggingEnabled:config.loggingEnabled];
    }
    
    switch (config.storageImageFormat) {
        case SBSDKImageFileFormatJPEG:
            SBSDKUIPageFileStorage.defaultStorage = [[SBSDKUIPageFileStorage alloc] initWithJPEGFileFormatAndCompressionQuality:config.storageImageQuality];
            break;

        case SBSDKImageFileFormatPNG:
            SBSDKUIPageFileStorage.defaultStorage = [[SBSDKUIPageFileStorage alloc] initWithImageFileFormat:SBSDKImageFileFormatPNG];
            break;

        default:
            reject(@"error", @"Unsupported image file format", nil);
            return;
    }
   
    RCTLogInfo(@"%@", initMessage);
    [SharedConfiguration defaultConfiguration].isSDKInitialized = YES;
    resolve(@{@"result":initMessage});
}

RCT_EXPORT_METHOD(isLicenseValid:(RCTPromiseResolveBlock)resolve
                         failure:(RCTPromiseRejectBlock)reject) {
    if (rejectIfUninitialized(reject)) {
        return;
    }
    
    RCTLogInfo(@"Validating ScanbotSDK license...");
    
    BOOL licenseValid = [ScanbotSDK isLicenseValid];
    resolve(@(licenseValid));
}

RCT_EXPORT_METHOD(applyImageFilter:(NSString *)imageFileUri
                  filter:(NSString *)filterType
                  success:(RCTPromiseResolveBlock)resolve
				  failure:(RCTPromiseRejectBlock)reject) {
    if (rejectIfUninitialized(reject)) {
        return;
    }

    NSString *outputImageFilePath = [ImageUtils generateTemporaryDocumentsFilePath:@"jpg"];
    NSURL *inputImageFileURL = [NSURL URLWithString:imageFileUri];
    NSData *data = [NSData dataWithContentsOfURL:inputImageFileURL];
    if (!data) {
        reject(@"error", @"File not found.", nil);
    } else {
        [SBSDKImageProcessor filterImage:[UIImage imageWithData:data]
                                  filter:filterIdFromFilterNameString(filterType)
                              completion:^(BOOL finished, NSError * _Nullable error, NSDictionary<NSString *,NSObject *> * _Nullable resultInfo) {
                                  if (finished && !error) {
                                      [ImageUtils saveImage:outputImageFilePath
                                                      image:(UIImage *)resultInfo[SBSDKResultInfoDestinationImageKey]
                                                    quality:SharedConfiguration.defaultConfiguration.sdkConfiguration.storageImageQuality];
                                      resolve(@{@"imageFileUri":[NSURL fileURLWithPath:outputImageFilePath].absoluteString});
                                  } else {
                                      reject(@"error", error.localizedDescription, error);
                                  }
                              }];
    }
}

RCT_EXPORT_METHOD(detectDocument:(nonnull NSString *)imageFileUri
                  success:(RCTPromiseResolveBlock)resolve
                  failure:(RCTPromiseRejectBlock)reject) {
    [RNScanbotSDK sbsdk_detectDocument:imageFileUri success:resolve failure:reject];
}

+ (void)sbsdk_detectDocument:(nonnull NSString *)imageFileUri
               success:(RCTPromiseResolveBlock)resolve
               failure:(RCTPromiseRejectBlock)reject {
    if (rejectIfUninitialized(reject)) {
        return;
    }

    UIImage *image = [ImageUtils loadImage:imageFileUri];
    if (!image) {
        reject(@"error", @"Document detection failed. Input image file does not exist.", nil);
    } else {
        SBSDKDocumentDetector *detector = [SBSDKDocumentDetector new];
        SBSDKDocumentDetectorResult *result = [detector detectDocumentPolygonOnImage:image
                                                                    visibleImageRect:CGRectZero
                                                                    smoothingEnabled:YES
                                                          useLiveDetectionParameters:NO];
        
        NSString *outputImageFilePath = [ImageUtils generateTemporaryDocumentsFilePath:@"jpg"];
        NSURL *outputImageURL = [NSURL fileURLWithPath:outputImageFilePath];
        
        if (result.polygon != nil && (result.status == SBSDKDocumentDetectionStatusOK ||
                                      result.status == SBSDKDocumentDetectionStatusOK_SmallSize ||
                                      result.status == SBSDKDocumentDetectionStatusOK_BadAngles ||
                                      result.status == SBSDKDocumentDetectionStatusOK_BadAspectRatio)) {
            [SBSDKImageProcessor warpImage:image
                                   polygon:result.polygon
                                completion:^(BOOL finished, NSError * _Nullable error, NSDictionary<NSString *,NSObject *> * _Nullable resultInfo) {
                                    if (finished && !error) {
                                        [ImageUtils saveImage:outputImageFilePath
                                                        image:(UIImage *)resultInfo[SBSDKResultInfoDestinationImageKey]
                                                      quality:SharedConfiguration.defaultConfiguration.sdkConfiguration.storageImageQuality];
                                        NSDictionary *callbackResult = @{@"documentImageFileUri":outputImageURL.absoluteString,
                                                                         @"detectionResult":[result.polygon detectionStatusString],
                                                                         @"polygon":[result.polygon polygonPoints]};
                                        resolve(callbackResult);
                                    } else {
                                        reject(@"error", error.localizedDescription, nil);
                                    }
                                }];
        } else {
            resolve(@{@"detectionResult":DetectionResultAsJSONStringValue(SBSDKDocumentDetectionStatusError_NothingDetected)});
        }
    }
}

RCT_EXPORT_METHOD(getOCRConfigs:(RCTPromiseResolveBlock)resolve
                        failure:(RCTPromiseRejectBlock)reject) {
    if (rejectIfUninitialized(reject)) {
        return;
    }
    NSMutableDictionary *resultDict = [NSMutableDictionary dictionary];
    NSURL *languageDataPathURL = [NSURL fileURLWithPath: [SBSDKOpticalTextRecognizer languageDataPath]];
    resultDict[@"languageDataPath"] = [languageDataPathURL absoluteString];
    resultDict[@"installedLanguages"] = [SBSDKOpticalTextRecognizer installedLanguages];
    resolve(resultDict);
}

RCT_EXPORT_METHOD(performOCR:(nonnull NSArray<NSString *> *)imageFileUris
                  languages:(nonnull NSArray<NSString *> *)languages
                  options:(nonnull NSDictionary *)options
                  success:(RCTPromiseResolveBlock)resolve
				  failure:(RCTPromiseRejectBlock)reject) {
    if (rejectIfUninitialized(reject)) {
        return;
    }

    JSONOptionsUtils *optionsUtils = [[JSONOptionsUtils alloc] initWithOptions:options];
	NSString *outputFormat = [optionsUtils stringValueForOption:@"outputFormat"
												   defaultValue:@"PLAIN_TEXT"];

    if (!languages) {
        reject(@"error", @"At least one language must be specified.", nil);
        return;
    }
    if (!imageFileUris) {
        reject(@"error", @"At least one image must be present.", nil);
        return;
    }

    NSArray *missingLanguages = [OCRUtils checkMissingLanguages:languages];
    if (missingLanguages.count > 0) {
        reject(@"error", [OCRUtils missingLanguagesStringFromArray:missingLanguages], nil);
        return;
    }

    NSURL *outputPDFURL = nil;
    if ([outputFormat isEqualToString:@"PDF_FILE"] || [outputFormat isEqualToString:@"FULL_OCR_RESULT"]) {
        outputPDFURL = [NSURL fileURLWithPath:[ImageUtils generateTemporaryDocumentsFilePath:@"pdf"]];
    }

    NSString *langString = languages.firstObject;
    for (NSInteger index = 1; index < languages.count; ++index) {
        langString = [NSString stringWithFormat:@"%@+%@", langString, languages[index]];
    }
    
    SBSDKIndexedImageStorage *tempStorage = [ImageUtils imageStorageFromFilesList:imageFileUris];

    [SBSDKOpticalTextRecognizer recognizeText:tempStorage
                             copyImageStorage:NO
                                     indexSet:nil
                               languageString:langString
                                 pdfOutputURL:outputPDFURL
                                   completion:^(BOOL finished, NSError *error, NSDictionary *resultInfo) {
                                       // cleaning up the temp storage dir!
                                       [tempStorage removeAllImages];
                                       if (finished && !error) {
                                           SBSDKOCRResult *result = resultInfo[SBSDKResultInfoOCRResultsKey];
                                           NSMutableDictionary *resultDict = [NSMutableDictionary dictionary];
                                           if ([outputFormat isEqualToString:@"PLAIN_TEXT"]) {
                                               resultDict[@"plainText"] = result.recognizedText;
                                           } else if ([outputFormat isEqualToString:@"PDF_FILE"]) {
                                               resultDict[@"pdfFileUri"] = outputPDFURL.absoluteString;
                                           } else if ([outputFormat isEqualToString:@"FULL_OCR_RESULT"]) {
                                               resultDict[@"plainText"] = result.recognizedText;
                                               resultDict[@"pdfFileUri"] = outputPDFURL.absoluteString;
                                           }
                                           resolve(resultDict);
                                       } else {
                                           reject(@"error", error.localizedDescription, nil);
                                       }
                                   }];
}

RCT_EXPORT_METHOD(createPDF:(NSArray *)imageFileUris
                  success:(RCTPromiseResolveBlock)resolve
				  failure:(RCTPromiseRejectBlock)reject) {
    if (rejectIfUninitialized(reject)) {
        return;
    }
    
    [ImageUtils recreateTempDirectoryIfNeeded];
    if (imageFileUris.count == 0) {
        reject(@"error", @"At least one image must be present.", nil);
        return;
    }
    
    NSString *outputPdfFilePath = [ImageUtils generateTemporaryDocumentsFilePath:@"pdf"];
    NSURL *pdfOutputURL = [NSURL fileURLWithPath:outputPdfFilePath];
    
    SBSDKIndexedImageStorage *tempStorage = [ImageUtils imageStorageFromFilesList:imageFileUris];
    
    [SBSDKPDFRenderer renderImageStorage:tempStorage
                        copyImageStorage:NO
                                indexSet:nil
                            withPageSize:SBSDKPDFRendererPageSizeAuto
                                  output:pdfOutputURL
                       completionHandler:^(BOOL finished, NSError *error, NSDictionary *resultInfo) {
                           // cleaning up the temp storage dir!
                           [tempStorage removeAllImages];
                           if (finished && !error) {
                               resolve(@{@"pdfFileUri":pdfOutputURL.absoluteString});
                           } else {
                               reject(@"error", error.localizedDescription, nil);
                           }
                       }];
}

RCT_EXPORT_METHOD(writeTIFF:(NSArray *)imageFileUris
                  options:(NSDictionary *)options
                  success:(RCTPromiseResolveBlock)resolve
                  failure:(RCTPromiseRejectBlock)reject) {
    if (rejectIfUninitialized(reject)) {
        return;
    }
    
    NSMutableArray* uris = [NSMutableArray arrayWithCapacity:[imageFileUris count]];
    for (NSString* uriStr in imageFileUris) {
        [uris addObject:[NSURL URLWithString:uriStr]];
    }
    
    NSURL *outputTiffFileUri = [NSURL fileURLWithPath:[ImageUtils generateTemporaryDocumentsFilePath:@"tiff"]];
    
    JSONOptionsUtils *optionsUtils = [[JSONOptionsUtils alloc] initWithOptions:options];
    BOOL binarized = [optionsUtils boolValueForOption:@"oneBitEncoded" defaultValue:FALSE];
    
    BOOL success = binarized
        ? [SBSDKTIFFImageWriter writeBinarizedMultiPageTIFFFromImageURLs:uris fileURL:outputTiffFileUri]
        : [SBSDKTIFFImageWriter writeMultiPageTIFFFromImageURLs:uris fileURL:outputTiffFileUri];

    if (!success) {
        reject(@"error", @"TIFF creation failed", nil);
    }
    
    resolve(@{@"tiffFileUri":outputTiffFileUri.absoluteString});
}

RCT_EXPORT_METHOD(rotateImage:(NSString *)imageFileUri
                  degrees:(nonnull NSNumber *)degrees
				  success:(RCTPromiseResolveBlock)resolve
				  failure:(RCTPromiseRejectBlock)reject) {
    if (rejectIfUninitialized(reject)) {
        return;
    }

    [ImageUtils recreateTempDirectoryIfNeeded];
    NSString *outputImageFilePath = [ImageUtils generateTemporaryDocumentsFilePath:@"jpg"];
    
    UIImage *inputImage = [ImageUtils loadImage:imageFileUri];
    if (inputImage) {
        UIImage *outputImage = [inputImage sbsdk_imageRotatedByDegrees:[degrees doubleValue]];
        if ([ImageUtils saveImage:outputImageFilePath
                            image:outputImage
                          quality:SharedConfiguration.defaultConfiguration.sdkConfiguration.storageImageQuality]) {
            resolve(@{@"imageFileUri":[NSURL fileURLWithPath:outputImageFilePath].absoluteString});
        } else {
            reject(@"error", @"Save image failed.", nil);
        }
    } else {
        reject(@"error", @"Image file not found.", nil);
    }
}


RCT_EXPORT_METHOD(createPage:(NSString*)imageFileUri
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject) {
    if (rejectIfUninitialized(reject)) {
        return;
    }

    UIImage* image = [ImageUtils loadImage:imageFileUri];
    if (!image) {
        reject(@"error", @"Image file not found.", nil);
    }
    image = [image sbsdk_imageWithFixedOrientation];
    NSUUID* pageId = [SBSDKUIPageFileStorage.defaultStorage addImage:image];
    SBSDKUIPage* page = [[SBSDKUIPage alloc] initWithPageFileID:pageId polygon:nil];
    resolve(dictionaryFromPage(page));
}

RCT_EXPORT_METHOD(detectDocumentOnPage:(NSDictionary*)pageDict
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject) {
    if (rejectIfUninitialized(reject)) {
        return;
    }

    SBSDKUIPage* page = pageFromDictionary(pageDict);
    [page detectDocument:true];
    resolve(dictionaryFromPage(page));
}

RCT_EXPORT_METHOD(applyImageFilterOnPage:(NSDictionary*)pageDict
                                  filter:(NSString *)filterType
                            withResolver:(RCTPromiseResolveBlock)resolve
                            withRejecter:(RCTPromiseRejectBlock)reject) {
    if (rejectIfUninitialized(reject)) {
        return;
    }
    
    SBSDKUIPage* page = pageFromDictionary(pageDict);
    page.filter = filterIdFromFilterNameString(filterType);
    resolve(dictionaryFromPage(page));
}

RCT_EXPORT_METHOD(rotatePage:(NSDictionary*)pageDict
                  times:(nonnull NSNumber *)times
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject) {
    if (rejectIfUninitialized(reject)) {
        return;
    }
    
    SBSDKUIPage* page = pageFromDictionary(pageDict);
    [page rotateClockwise:-[times integerValue]];
    resolve(dictionaryFromPage(page));
}

RCT_EXPORT_METHOD(getFilteredDocumentPreviewUri:(NSDictionary*)pageDict
                                         filter:(NSString *)filterType
                                   withResolver:(RCTPromiseResolveBlock)resolve
                                   withRejecter:(RCTPromiseRejectBlock)reject) {
    if (rejectIfUninitialized(reject)) {
        return;
    }

    SBSDKUIPage* page = pageFromDictionary(pageDict);
    SBSDKImageFilterType filter = filterIdFromFilterNameString(filterType);
    resolve(uriWithMinihash([page documentPreviewImageURLUsingFilter:filter]));
}

/*
 * ! getStoredPages() method dropped, due to potential abuse or wrong usage !
 * It provides page objects without meta data (polygon, filter, etc),
 * which may cause inconsistency (especially in our iOS SDK, e.g. rotatePage() method)!
RCT_EXPORT_METHOD(getStoredPages:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject) {
    if (rejectIfUninitialized(reject)) {
        return;
    }

    NSArray<NSUUID *>* pageIds = [SBSDKUIPageFileStorage.defaultStorage allPageFileIDs];
    NSMutableArray<NSDictionary*>* pages = [NSMutableArray arrayWithCapacity:[pageIds count]];
    
    for (NSUUID* pageId in pageIds)
    {
        [pages addObject:dictionaryFromPage([[SBSDKUIPage alloc] initWithPageFileID:pageId polygon:nil])];
    }
    resolve(pages);
}
*/

RCT_EXPORT_METHOD(removePage:(NSDictionary*)pageDict
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject) {
    if (rejectIfUninitialized(reject)) {
        return;
    }

    SBSDKUIPage* page = pageFromDictionary(pageDict);
    [SBSDKUIPageFileStorage.defaultStorage removePageFileID:page.pageFileUUID];
    resolve(nil);
}

RCT_EXPORT_METHOD(setDocumentImage:(NSDictionary*)pageDict
                  imageFileUri:(NSString*)imageFileUri
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject) {
    if (rejectIfUninitialized(reject)) {
        return;
    }

    SBSDKUIPage* page = pageFromDictionary(pageDict);
    NSError* error = nil;
    if ([SBSDKUIPageFileStorage.defaultStorage setImage:[ImageUtils loadImage:imageFileUri]
                                  forExistingPageFileID:page.pageFileUUID
                                           pageFileType:SBSDKUIPageFileTypeDocument
                                                  error:&error] && !error) {
        resolve(dictionaryFromPage(page));
    } else {
        reject(@"error", @"Could not set document image", error);
    }
}

RCT_EXPORT_METHOD(cleanup:(RCTPromiseResolveBlock)resolve
                  failure:(RCTPromiseRejectBlock)reject) {
    if (rejectIfUninitialized(reject)) {
        return;
    }
    
    [SBSDKUIPageFileStorage.defaultStorage removeAll];
    NSError *cleanupError = [ImageUtils removeAllFilesFromTemporaryDocumentsDirectory];
    if (!cleanupError) {
        resolve(nil);
    } else {
        reject(@"error", cleanupError.localizedDescription, cleanupError);
    }
}

RCT_EXPORT_METHOD(recognizeMrz:(nonnull NSString *)imageFileUri
                  success:(RCTPromiseResolveBlock)resolve
                  failure:(RCTPromiseRejectBlock)reject) {
    if (rejectIfUninitialized(reject)) {
        return;
    }

    UIImage *image = [ImageUtils loadImage:imageFileUri];
    if (!image) {
        reject(@"error", @"MRZ recognition failed. Input image file does not exist.", nil);
        return;
    }

    SBSDKMachineReadableZoneRecognizer *recognizer = [SBSDKMachineReadableZoneRecognizer new];
    SBSDKMachineReadableZoneRecognizerResult *mrzResult = [recognizer recognizePersonalIdentityFromImage:image];

    NSDictionary* result = MRZRecognizerResultAsDictionary(mrzResult);
    resolve(result);
}

@end
