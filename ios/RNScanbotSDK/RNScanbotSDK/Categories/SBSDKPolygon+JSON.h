//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

@import ScanbotSDK;

@interface SBSDKPolygon (JSON)

- (NSArray *)polygonPoints;
- (NSString *)detectionStatusString;
- (NSString *)detectionStatusStringFromSBSDKStatus:(SBSDKDocumentDetectionStatus)status;

@end
