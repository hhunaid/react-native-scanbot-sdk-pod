//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2018 doo GmbH. All rights reserved.
//

@import ScanbotSDK;

NSString* DetectionResultAsJSONStringValue(SBSDKDocumentDetectionStatus status);
NSString* ImageFilterAsJSONStringValue(SBSDKImageFilterType filter);
SBSDKImageFilterType filterIdFromFilterNameString(NSString* filterName);

NSString* MRZFieldNameAsString(SBSDKMachineReadableZoneRecognizerFieldName name);
NSDictionary* dictionaryFromPage(SBSDKUIPage* page);
SBSDKUIPage* pageFromDictionary(NSDictionary* dict);

NSString* MRZDocumentTypeAsString(SBSDKMachineReadableZoneRecognizerResultDocumentType documentType);
NSDictionary* MRZRecognizerResultAsDictionary(SBSDKMachineReadableZoneRecognizerResult* mrzResult);
