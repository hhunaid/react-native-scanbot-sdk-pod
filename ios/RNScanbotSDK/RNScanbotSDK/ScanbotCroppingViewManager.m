//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import "ScanbotCroppingViewManager.h"
#import "ScanbotCroppingView.h"

@interface ScanbotCroppingViewManager ()

@property (nonatomic, strong) ScanbotCroppingView *croppingView;

@end

@implementation ScanbotCroppingViewManager

RCT_EXPORT_MODULE();

static ScanbotCroppingView *croppingView;

+ (ScanbotCroppingView *)croppingView {
	@synchronized(self) {
		return croppingView;
	}
}

+ (void)setCroppingView:(ScanbotCroppingView *)newView {
	@synchronized(self) {
		croppingView = newView;
	}
}

- (UIView *)view {
	ScanbotCroppingViewManager.croppingView = [ScanbotCroppingView new];
	return ScanbotCroppingViewManager.croppingView;
}

RCT_EXPORT_VIEW_PROPERTY(edgeColor, NSString);
RCT_EXPORT_VIEW_PROPERTY(imageFileUri, NSString);
RCT_EXPORT_VIEW_PROPERTY(imageCompressionQuality, NSNumber);
RCT_EXPORT_VIEW_PROPERTY(cancelButtonImageUri, NSString);
RCT_EXPORT_VIEW_PROPERTY(applyButtonImageUri, NSString);
RCT_EXPORT_VIEW_PROPERTY(onChangesAppliedWithPolygon, RCTBubblingEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onChangesCanceled, RCTBubblingEventBlock);

@end
