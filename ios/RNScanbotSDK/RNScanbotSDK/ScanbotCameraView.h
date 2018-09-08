//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <React/RCTComponent.h>

@interface ScanbotCameraView : UIView

@property (nonatomic, copy) RCTBubblingEventBlock onImageCaptured;
@property (nonatomic, copy) RCTBubblingEventBlock onImageCaptureFailed;
@property (nonatomic, copy) RCTBubblingEventBlock onDocumentImageCaptured;
@property (nonatomic, copy) RCTBubblingEventBlock onPolygonDetected;
@property (nonatomic, copy) RCTBubblingEventBlock onStartCapturingImage;

@property (nonatomic) BOOL autoSnappingEnabled;
@property (nonatomic) NSString *edgeColor;
@property (nonatomic) NSInteger imageCompressionQuality;
@property (nonatomic) NSInteger sampleSize;
@property (nonatomic) CGFloat autoSnappingSensitivity;
@property (nonatomic) NSDictionary *textResBundle;

@end
