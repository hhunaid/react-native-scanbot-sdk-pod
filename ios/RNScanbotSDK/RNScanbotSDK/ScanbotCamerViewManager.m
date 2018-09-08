//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import "ScanbotCamerViewManager.h"
#import <React/UIView+React.h>
#import "ScanbotCameraView.h"

@implementation ScanbotCamerViewManager

RCT_EXPORT_MODULE(ScanbotCameraView)

- (UIView *)view {
  return [ScanbotCameraView new];
}

RCT_EXPORT_VIEW_PROPERTY(edgeColor, NSString);
RCT_EXPORT_VIEW_PROPERTY(autoSnappingEnabled, BOOL);
RCT_EXPORT_VIEW_PROPERTY(imageCompressionQuality, NSInteger);
RCT_EXPORT_VIEW_PROPERTY(autoSnappingSensitivity, CGFloat);
RCT_EXPORT_VIEW_PROPERTY(sampleSize, NSInteger);
RCT_EXPORT_VIEW_PROPERTY(textResBundle, NSDictionary);

RCT_EXPORT_VIEW_PROPERTY(onImageCaptured, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onImageCaptureFailed, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onDocumentImageCaptured, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onPolygonDetected, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onStartCapturingImage, RCTBubblingEventBlock);

@end
