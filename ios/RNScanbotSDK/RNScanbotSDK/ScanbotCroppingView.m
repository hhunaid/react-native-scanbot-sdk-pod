//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import "ScanbotCroppingView.h"
#import "UIColor+JSON.h"
#import "SBSDKPolygon+JSON.h"
#import "ImageUtils.h"
#import <React/UIView+React.h>
#import "SharedConfiguration.h"

@import ScanbotSDK;

@interface ScanbotCroppingView () <SBSDKImageEditingViewControllerDelegate>

@property (nonatomic, strong) SBSDKImageEditingViewController *imageEditingController;
@property (nonatomic, strong) NSString *uniqueReceiverId;

@end

@implementation ScanbotCroppingView

- (void)setupInitialParameters {
  if ([self.imageCompressionQuality integerValue] <= 0 || [self.imageCompressionQuality integerValue] > 100) {
    self.imageCompressionQuality = @(kDefaultImageCompressionQuality);
  }
  
  if (!self.edgeColor) {
    self.edgeColor = @"#ff80cbc4";
  }
  self.imageEditingController.edgeColor = [UIColor colorFromHexString:self.edgeColor];
  self.imageEditingController.magneticEdgeColor = [UIColor colorFromHexString:self.edgeColor];
}

- (instancetype)init {
  if (self = [super init]) {
    self.imageEditingController = [SBSDKImageEditingViewController new];
    self.imageEditingController.delegate = self;
    [self setupInitialParameters];
    [self addSubview:self.imageEditingController.view];
  }
  return self;
}

- (void)reactSetFrame:(CGRect)frame {
  [super reactSetFrame:frame];
  self.imageEditingController.view.frame = self.bounds;
}

- (void)applyCroppingChanges {
	[self.imageEditingController applyChanges];
}

- (void)dismissCroppingChanges {
	[self.imageEditingController dismissChanges];
}

- (void)rotateImageClockwise {
	if (self.imageEditingController) {
		[self.imageEditingController rotateInputImageClockwise:YES animated:YES];
	}
}

#pragma mark - Prop setters

- (void)setImageFileUri:(NSString *)imageFileUri {
	if (![_imageFileUri isEqualToString:imageFileUri]) {
		_imageFileUri = imageFileUri;
	}
	self.imageEditingController.image = [ImageUtils loadImage:self.imageFileUri];
}

- (void)setEdgeColor:(NSString *)edgeColor {
	_edgeColor = edgeColor;
	self.imageEditingController.edgeColor = [UIColor colorFromHexString:_edgeColor];
	self.imageEditingController.magneticEdgeColor = [UIColor colorFromHexString:_edgeColor];
}

#pragma mark - SBSDKImageEditingViewController delegate

- (UIBarStyle)imageEditingViewControllerToolbarStyle:(SBSDKImageEditingViewController *)editingViewController {
	return UIBarStyleDefault;
}

- (void)imageEditingViewController:(SBSDKImageEditingViewController *)editingViewController
		didApplyChangesWithPolygon:(SBSDKPolygon *)polygon
					  croppedImage:(UIImage *)croppedImage {
	NSString *outputImageFilePath = [ImageUtils generateTemporaryDocumentsFilePath:@"jpg"];
	NSURL *outputImageURL = [NSURL fileURLWithPath:outputImageFilePath];
	if ([ImageUtils saveImage:outputImageFilePath
						image:croppedImage
					  quality:[self.imageCompressionQuality integerValue]]) {
		NSDictionary *callbackResult = @{@"imageFileUri":outputImageURL.absoluteString,
										 @"polygon":[polygon polygonPoints]};
		if (self.onChangesAppliedWithPolygon) {
			self.onChangesAppliedWithPolygon(callbackResult);
		}
	} else {
		if (self.onChangesAppliedWithPolygon) {
			self.onChangesAppliedWithPolygon(@{@"error":@"Save image failed."});
		}
	}
}

- (void)imageEditingViewControllerDidCancelChanges:(SBSDKImageEditingViewController *)editingViewController {
	if (self.onChangesCanceled) {
		self.onChangesCanceled(@{});
	}
}

- (UIBarButtonItem *)imageEditingViewControllerApplyButtonItem:(SBSDKImageEditingViewController *)editingViewController {
	return [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"ui_action_checkmark"]
											style:UIBarButtonItemStylePlain
										   target:nil
										   action:NULL];
}

- (UIBarButtonItem *)imageEditingViewControllerCancelButtonItem:(SBSDKImageEditingViewController *)editingViewController {
	return [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"ui_action_close"]
											style:UIBarButtonItemStylePlain
										   target:nil
										   action:NULL];
}

@end
