//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import "ScanbotCroppingViewModule.h"
#import "ScanbotCroppingViewManager.h"

@implementation ScanbotCroppingViewModule

RCT_EXPORT_MODULE(ScanbotCroppingViewModule);

RCT_EXPORT_METHOD(applyCroppingChanges) {
	dispatch_async(dispatch_get_main_queue(), ^{
		if (ScanbotCroppingViewManager.croppingView) {
			[ScanbotCroppingViewManager.croppingView applyCroppingChanges];
		}
	});
}

RCT_EXPORT_METHOD(dismissCroppingChanges) {
	dispatch_async(dispatch_get_main_queue(), ^{
		if (ScanbotCroppingViewManager.croppingView) {
			[ScanbotCroppingViewManager.croppingView dismissCroppingChanges];
		}
	});
}

RCT_EXPORT_METHOD(rotateImageClockwise) {
	dispatch_async(dispatch_get_main_queue(), ^{
		if (ScanbotCroppingViewManager.croppingView) {
			[ScanbotCroppingViewManager.croppingView rotateImageClockwise];
		}
	});
}

@end
