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
#import "SBSDKPolygon+JSON.h"
#import "ScanbotCroppingView.h"
#import "JSONOptionsUtils.h"

@import ScanbotSDK;

@implementation RNScanbotSDK

RCT_EXPORT_MODULE(ScanbotSDK);

- (CGFloat)validateCompressionQuality:(NSNumber *)compressionQuality {
	if (!compressionQuality ||
		[compressionQuality integerValue] < 0 ||
		[compressionQuality integerValue] > 100) {
		return kDefaultImageCompressionQuality;
	} else {
		return [compressionQuality integerValue];
	}
}

#pragma mark - Private methods implementations

- (void)sbsdk_initializeSDK:(NSString *)licenseKey
			 loggingEnabled:(BOOL)loggingEnabled
					success:(RCTResponseSenderBlock)success
					failure:(RCTResponseSenderBlock)failure {
    BOOL hasLicense = NO;
    if (licenseKey && ![licenseKey isEqualToString:@""]) {
        hasLicense = [ScanbotSDK setLicense:licenseKey];
		if (!hasLicense) {
			if (failure) {
				failure(@[@{@"error":@"License key is invalid."}]);
			}
			return;
		}
    }
    if (loggingEnabled) {
        [ScanbotSDK setLoggingEnabled:loggingEnabled];
        [SharedConfiguration defaultConfiguration].loggingEnabled = loggingEnabled;
    }
    [ScanbotSDK setSharedApplication:[UIApplication sharedApplication]];
    
    NSString *initMessage = @"Scanbot SDK initialized.";
    RCTLogInfo(@"%@", initMessage);
    if (!hasLicense) {
        initMessage = @"Trial mode activated. You can now test all features for 60 seconds.";
        RCTLogInfo(@"%@", initMessage);
    }
    
    [SharedConfiguration defaultConfiguration].isSDKInitialized = YES;
    if (success) {
        success(@[@{@"result":initMessage}]);
    }
}

- (void)sbsdk_isLicenseValid:(RCTResponseSenderBlock)success
                	 failure:(RCTResponseSenderBlock)failure {
    if (![SharedConfiguration isSDKInitialized]) {
        return;
    }
    
    RCTLogInfo(@"Validating ScanbotSDK license...");
    
    NSString *resultMessage = nil;
	BOOL licenseValid = [ScanbotSDK isLicenseValid];
	
    if (licenseValid) {
        resultMessage = @"ScanbotSDK license is valid";
	} else {
        resultMessage = @"ScanbotSDK license is not valid";
    }
    RCTLogInfo(@"%@", resultMessage);
	success(@[@{@"isLicenseValid":@(licenseValid)}]);
}

- (SBSDKImageFilterType)filterIdFromFilterNameString:(NSString *)filterName {
    if ([filterName isEqualToString:@"COLOR_ENHANCED"]) return SBSDKImageFilterTypeColor;
    if ([filterName isEqualToString:@"GRAYSCALE"]) return SBSDKImageFilterTypeGray;
    if ([filterName isEqualToString:@"BINARIZED"]) return SBSDKImageFilterTypeBinarized;
	if ([filterName isEqualToString:@"COLOR_DOCUMENT"]) return SBSDKImageFilterTypeColorDocument;
    return SBSDKImageFilterTypeNone;
}

- (void)sbsdk_applyImageFilter:(NSString *)filterType
				  imageFileUri:(NSString *)imageFileUri
			compressionQuality:(NSNumber *)compressionQuality
					   success:(RCTResponseSenderBlock)success
					   failure:(RCTResponseSenderBlock)failure {
    if (![SharedConfiguration isSDKInitialized]) {
        return;
    }
    
    NSString *outputImageFilePath = [ImageUtils generateTemporaryDocumentsFilePath:@"jpg"];
    NSURL *inputImageFileURL = [NSURL URLWithString:imageFileUri];
	NSData *data = [NSData dataWithContentsOfURL:inputImageFileURL];
	if (!data) {
		failure(@[@{@"error":@"File not found."}]);
	} else {
		[SBSDKImageProcessor filterImage:[UIImage imageWithData:data]
								  filter:[self filterIdFromFilterNameString:filterType]
							  completion:^(BOOL finished, NSError * _Nullable error, NSDictionary<NSString *,NSObject *> * _Nullable resultInfo) {
								  if (finished && !error) {
									  [ImageUtils saveImage:outputImageFilePath
													  image:(UIImage *)resultInfo[SBSDKResultInfoDestinationImageKey]
													quality:[self validateCompressionQuality:compressionQuality]];
									  success(@[@{@"imageFileUri":[NSURL fileURLWithPath:outputImageFilePath].absoluteString}]);
								  } else {
									  failure(@[@{@"error":error.localizedDescription}]);
								  }
							  }];
	}
}

- (void)sbsdk_detectDocument:(NSString *)imageUri
		  compressionQuality:(NSNumber *)compressionQuality
					 success:(RCTResponseSenderBlock)success
					 failure:(RCTResponseSenderBlock)failure {
    if (![SharedConfiguration isSDKInitialized]) {
        return;
    }
    
    if ([imageUri isKindOfClass:[NSNull class]]) {
        return;
    }
    
    UIImage *image = [ImageUtils loadImage:imageUri];
    if (!image) {
        failure(@[@{@"error":@"Document detection failed. Input image file does not exist."}]);
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
                                                      quality:[self validateCompressionQuality:compressionQuality]];
                                        NSDictionary *callbackResult = @{@"imageFileUri":outputImageURL.absoluteString,
                                                                         @"detectionResult":[result.polygon detectionStatusString],
                                                                         @"polygon":[result.polygon polygonPoints]};
                                        success(@[callbackResult]);
                                    } else {
                                        failure(@[@{@"error":error.localizedDescription}]);
                                    }
                                }];
        } else {
            NSString *detectionStatusString = [[SBSDKPolygon new] detectionStatusStringFromSBSDKStatus:SBSDKDocumentDetectionStatusError_NothingDetected];
            NSDictionary *callbackResult = @{@"imageFileUri":[NSNull null],
                                             @"detectionResult":detectionStatusString,
                                             @"polygon":[NSArray array]};
            success(@[callbackResult]);
        }
    }
}

- (void)sbsdk_getOCRConfigs:(RCTResponseSenderBlock)success
                    failure:(RCTResponseSenderBlock)failure {
    NSMutableDictionary *resultDict = [NSMutableDictionary dictionary];
    NSURL *languageDataPathURL = [NSURL fileURLWithPath: [SBSDKOpticalTextRecognizer languageDataPath]];
    resultDict[@"languageDataPath"] = [languageDataPathURL absoluteString];
    resultDict[@"installedLanguages"] = [SBSDKOpticalTextRecognizer installedLanguages];
    success(@[resultDict]);
}

- (void)sbsdk_performOCR:(NSArray *)imageFileUris
			   languages:(NSArray *)languages
			outputFormat:(NSString *)outputFormat
				 success:(RCTResponseSenderBlock)success
				 failure:(RCTResponseSenderBlock)failure {
    if (![SharedConfiguration isSDKInitialized]) {
        return;
    }
    
    if (!languages) {
        failure(@[@{@"error":@"At least one language must be specified."}]);
        return;
    }
    if (!imageFileUris) {
        failure(@[@{@"error":@"At least one image must be present."}]);
        return;
    }
    
    NSArray *missingLanguages = [OCRUtils checkMissingLanguages:languages];
    if (missingLanguages.count > 0) {
        failure(@[@{@"error":[OCRUtils missingLanguagesStringFromArray:missingLanguages]}]);
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
    
    [SBSDKOpticalTextRecognizer recognizeText:[ImageUtils imageStorageFromFilesList:imageFileUris]
                             copyImageStorage:YES
                                     indexSet:nil
                               languageString:langString
                                 pdfOutputURL:outputPDFURL
                                   completion:^(BOOL finished, NSError *error, NSDictionary *resultInfo) {
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
                                           success(@[resultDict]);
                                       } else {
                                           failure(@[@{@"error":error.localizedDescription}]);
                                       }
                                   }];
}

- (void)sbsdk_createPDF:(NSArray *)imageFileUris
				success:(RCTResponseSenderBlock)success
				failure:(RCTResponseSenderBlock)failure {
    if (![SharedConfiguration isSDKInitialized]) {
        return;
    }
    
    [ImageUtils recreateTempDirectoryIfNeeded];
    if (imageFileUris.count == 0) {
        failure(@[@{@"error":@"At least one image must be present."}]);
        return;
    }
    
    NSIndexSet *indexSet = [NSIndexSet indexSetWithIndexesInRange:NSMakeRange(0, imageFileUris.count)];
    SBSDKPDFRendererPageSize sdkPageSize = SBSDKPDFRendererPageSizeAuto;
    
    NSString *outputPdfFilePath = [ImageUtils generateTemporaryDocumentsFilePath:@"pdf"];
    NSURL *pdfOutputURL = [NSURL fileURLWithPath:outputPdfFilePath];
    
    [SBSDKPDFRenderer renderImageStorage:[ImageUtils imageStorageFromFilesList:imageFileUris]
                        copyImageStorage:NO
                                indexSet:indexSet
                            withPageSize:sdkPageSize
                                  output:pdfOutputURL
                       completionHandler:^(BOOL finished, NSError *error, NSDictionary *resultInfo) {
                           dispatch_async(dispatch_get_main_queue(), ^{
                               if (finished && !error) {
                                   success(@[@{@"pdfFileUri":pdfOutputURL.absoluteString}]);
                               } else {
                                   failure(@[@{@"error":error.localizedDescription}]);
                               }
                           });
                       }];
    
}

- (void)sbsdk_rotateImage:(NSString *)imageFileUri
				  degrees:(NSNumber *)degrees
	   compressionQuality:(NSNumber *)compressionQuality
				  success:(RCTResponseSenderBlock)success
				  failure:(RCTResponseSenderBlock)failure {
	if (![SharedConfiguration isSDKInitialized]) {
		return;
	}
	
	[ImageUtils recreateTempDirectoryIfNeeded];
	NSString *outputImageFilePath = [ImageUtils generateTemporaryDocumentsFilePath:@"jpg"];
	
	UIImage *inputImage = [ImageUtils loadImage:imageFileUri];
	if (inputImage) {
		UIImage *outputImage = [inputImage sbsdk_imageRotatedByDegrees:[degrees integerValue]];
		if ([ImageUtils saveImage:outputImageFilePath
							image:outputImage
						  quality:[self validateCompressionQuality:compressionQuality]]) {
			success(@[@{@"imageFileUri":[NSURL fileURLWithPath:outputImageFilePath].absoluteString}]);
		} else {
			failure(@[@{@"error":@"Save image failed."}]);
		}
	} else {
		failure(@[@{@"error":@"Image file not found."}]);
	}
}

- (void)sbsdk_cleanup:(RCTResponseSenderBlock)success
			  failure:(RCTResponseSenderBlock)failure {
    NSError *cleanupError = [ImageUtils removeAllFilesFromTemporaryDocumentsDirectory];
    if (!cleanupError) {
        success(@[@{@"result":@"ok"}]);
    } else {
        failure(@[@{@"error":cleanupError.localizedDescription}]);
    }
}

#pragma mark - ReactNative exported interfaces

RCT_EXPORT_METHOD(initializeSDK:(NSDictionary *)options
				  success:(RCTResponseSenderBlock)success
				  failure:(RCTResponseSenderBlock)failure) {
	JSONOptionsUtils *optionsUtils = [[JSONOptionsUtils alloc] initWithOptions:options];
	NSString *licenseKey = [optionsUtils stringValueForOption:@"licenseKey"
												 defaultValue:@""];
	BOOL loggingEnabled = [optionsUtils boolValueForOption:@"loggingEnabled"
											  defaultValue:NO];
    [self sbsdk_initializeSDK:licenseKey
               loggingEnabled:loggingEnabled
					  success:success
					  failure:failure];
}

RCT_EXPORT_METHOD(isLicenseValid:(RCTResponseSenderBlock)success
                         failure:(RCTResponseSenderBlock)failure) {
    [self sbsdk_isLicenseValid:success
                       failure:failure];
}

RCT_EXPORT_METHOD(applyImageFilter:(NSDictionary *)options
                  success:(RCTResponseSenderBlock)success
				  failure:(RCTResponseSenderBlock)failure) {
	JSONOptionsUtils *optionsUtils = [[JSONOptionsUtils alloc] initWithOptions:options];
	NSString *filterType = [optionsUtils stringValueForOption:@"filterType"
												 defaultValue:@""];
	NSString *imageFileUri = [optionsUtils stringValueForOption:@"imageFileUri"
												   defaultValue:@""];
	NSNumber *imageCompressionQuality = @([optionsUtils integerValueForOption:@"imageCompressionQuality"
																 defaultValue:kDefaultImageCompressionQuality]);
	[self sbsdk_applyImageFilter:filterType
                    imageFileUri:imageFileUri
			  compressionQuality:imageCompressionQuality
						 success:success
						 failure:failure];
}

RCT_EXPORT_METHOD(detectDocument:(NSDictionary *)options
                  success:(RCTResponseSenderBlock)success
				  failure:(RCTResponseSenderBlock)failure) {
	JSONOptionsUtils *optionsUtils = [[JSONOptionsUtils alloc] initWithOptions:options];
	NSString *imageFileUri = [optionsUtils stringValueForOption:@"imageFileUri"
												   defaultValue:nil];
	NSNumber *imageCompressionQuality = @([optionsUtils integerValueForOption:@"imageCompressionQuality"
																 defaultValue:kDefaultImageCompressionQuality]);
    [self sbsdk_detectDocument:imageFileUri
			compressionQuality:imageCompressionQuality
					   success:success
					   failure:failure];
}

RCT_EXPORT_METHOD(getOCRConfigs:(RCTResponseSenderBlock)success
                        failure:(RCTResponseSenderBlock)failure) {
    [self sbsdk_getOCRConfigs:success
                      failure:failure];
}

RCT_EXPORT_METHOD(performOCR:(NSDictionary *)options
                  success:(RCTResponseSenderBlock)success
				  failure:(RCTResponseSenderBlock)failure) {
	JSONOptionsUtils *optionsUtils = [[JSONOptionsUtils alloc] initWithOptions:options];
	NSArray *imageFileUris = [optionsUtils arrayValueForOption:@"imageFileUris"
												  defaultValue:@[]];
	NSArray *languages = [optionsUtils arrayValueForOption:@"languages"
											  defaultValue:@[]];
	NSString *outputFormat = [optionsUtils stringValueForOption:@"outputFormat"
												   defaultValue:@"PLAIN_TEXT"];
    [self sbsdk_performOCR:imageFileUris
                 languages:languages
              outputFormat:outputFormat
				   success:success
				   failure:failure];
}

RCT_EXPORT_METHOD(createPDF:(NSDictionary *)options
                  success:(RCTResponseSenderBlock)success
				  failure:(RCTResponseSenderBlock)failure) {
	JSONOptionsUtils *optionsUtils = [[JSONOptionsUtils alloc] initWithOptions:options];
	NSArray *imageFileUris = [optionsUtils arrayValueForOption:@"imageFileUris"
												  defaultValue:@[]];
	[self sbsdk_createPDF:imageFileUris
				  success:success
				  failure:failure];
}

RCT_EXPORT_METHOD(cleanup:(RCTResponseSenderBlock)success
				  failure:(RCTResponseSenderBlock)failure) {
    [self sbsdk_cleanup:success
				failure:failure];
}

RCT_EXPORT_METHOD(rotateImage:(NSDictionary *)options
				  success:(RCTResponseSenderBlock)success
				  failure:(RCTResponseSenderBlock)failure) {
	JSONOptionsUtils *optionsUtils = [[JSONOptionsUtils alloc] initWithOptions:options];
	NSString *imageFileUri = [optionsUtils stringValueForOption:@"imageFileUri"
												   defaultValue:nil];
	NSNumber *degrees = @([optionsUtils integerValueForOption:@"degrees"
												 defaultValue:0]);
	NSNumber *imageCompressionQuality = @([optionsUtils integerValueForOption:@"imageCompressionQuality"
																 defaultValue:kDefaultImageCompressionQuality]);
	[self sbsdk_rotateImage:imageFileUri
					degrees:degrees
		 compressionQuality:imageCompressionQuality
					success:success
					failure:failure];
}


@end
