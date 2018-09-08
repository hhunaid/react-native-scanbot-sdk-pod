//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import "ScanbotCameraView.h"
#import "UIColor+JSON.h"
#import "SBSDKPolygon+JSON.h"
#import <React/UIView+React.h>
#import "ImageUtils.h"
#import "SharedConfiguration.h"
#import "JSONOptionsUtils.h"

@import ScanbotSDK;

@interface ScanbotCameraView () <SBSDKScannerViewControllerDelegate>

@property (strong, nonatomic) SBSDKScannerViewController *scannerViewController;
@property (assign, nonatomic) BOOL viewAppeared;
@property (nonatomic, strong) NSString *originalImageFileUri;

@property (nonatomic, strong) UIButton *flashButton;
@property (nonatomic, strong) UIButton *autoSnapButton;

@property (nonatomic, strong) SBSDKPolygon *lastDetectedPolygon;

@end

@implementation ScanbotCameraView

- (void)setupInitialParameters {
	self.originalImageFileUri = @"";
	if (self.imageCompressionQuality <= 0 || self.imageCompressionQuality > 100) {
		self.imageCompressionQuality = kDefaultImageCompressionQuality;
	}
	
	if (!self.edgeColor) {
		self.edgeColor = @"#ff80cbc4";
	}
	self.scannerViewController.autoCaptureSensitivity = self.autoSnappingSensitivity;
}

- (instancetype)init {
	if (self = [super init]) {
		self.scannerViewController = [SBSDKScannerViewController new];
		self.scannerViewController.delegate = self;
		[self setupInitialParameters];
		self.viewAppeared = YES;
		[self addSubview:self.scannerViewController.view];
	}
	return self;
}

- (void)reactSetFrame:(CGRect)frame {
	[super reactSetFrame:frame];
	self.scannerViewController.view.frame = self.bounds;
	
	[self placeFlashButton];
	[self placeAutoSnapButton];
}

- (void)placeAutoSnapButton {
	CGSize screenSize = self.bounds.size;
	CGRect buttonFrame = CGRectMake(50, screenSize.height - 80, 40, 40);
	if (!self.autoSnapButton) {
		self.autoSnapButton = [[UIButton alloc] initWithFrame:buttonFrame];
		[self.autoSnapButton setImage:[UIImage imageNamed:@"ui_autosnap_on"]
												 forState:UIControlStateSelected];
		[self.autoSnapButton setImage:[UIImage imageNamed:@"ui_autosnap_off"]
												 forState:UIControlStateNormal];
		[self.autoSnapButton addTarget:self
														action:@selector(autoSnapButtonTapped:)
									forControlEvents:UIControlEventTouchUpInside];
  
		[self addSubview:self.autoSnapButton];
		[self bringSubviewToFront:self.autoSnapButton];
	} else {
		[self.autoSnapButton setFrame:buttonFrame];
	}
	
	[self.autoSnapButton setSelected:self.autoSnappingEnabled];
	if (self.autoSnappingEnabled) {
		self.scannerViewController.shutterMode = SBSDKShutterModeSmart;
	} else {
		self.scannerViewController.shutterMode = SBSDKShutterModeAlwaysManual;
	}
}

- (void)placeFlashButton {
	CGSize screenSize = self.bounds.size;
	CGRect buttonFrame = CGRectMake(screenSize.width - 80, screenSize.height - 80, 40, 40);
	if (!self.flashButton) {
		self.flashButton = [[UIButton alloc] initWithFrame:buttonFrame];
		
		[self.flashButton setImage:[UIImage imageNamed:@"ui_flash_on"]
											forState:UIControlStateSelected];
		[self.flashButton setImage:[UIImage imageNamed:@"ui_flash_off"]
											forState:UIControlStateNormal];
		[self.flashButton addTarget:self
												 action:@selector(flashButtonTapped:)
							 forControlEvents:UIControlEventTouchUpInside];
		[self.flashButton setSelected:self.scannerViewController.cameraSession.isTorchLightEnabled];
		
		[self addSubview:self.flashButton];
		[self bringSubviewToFront:self.flashButton];
	} else {
		[self.flashButton setFrame:buttonFrame];
	}
}

- (void)flashButtonTapped:(id)sender {
	self.scannerViewController.cameraSession.torchLightEnabled = !self.scannerViewController.cameraSession.isTorchLightEnabled;
	[self.flashButton setSelected:self.scannerViewController.cameraSession.isTorchLightEnabled];
}

- (void)updateFlashButtonStatus {
	if (self.flashButton) {
		[self.flashButton setSelected:self.scannerViewController.cameraSession.isTorchLightEnabled];
	}
}

- (void)reenableAutosnapButton {
	self.autoSnapButton.userInteractionEnabled = YES;
}

- (void)autoSnapButtonTapped:(id)sender {
	self.autoSnappingEnabled = !self.autoSnappingEnabled;
	if (self.autoSnapButton) {
		[self.autoSnapButton setSelected:self.autoSnappingEnabled];
	}
	
	if (self.autoSnappingEnabled) {
		self.scannerViewController.shutterMode = SBSDKShutterModeSmart;
	} else {
		self.scannerViewController.shutterMode = SBSDKShutterModeAlwaysManual;
	}
	
	// avoid autosnap button tapping while shutter button is still animating it's state
	self.autoSnapButton.userInteractionEnabled = NO;
	[self performSelector:@selector(reenableAutosnapButton)
						 withObject:nil
						 afterDelay:0.5f];
}

#pragma mark - Prop setters

- (void)setAutoSnappingEnabled:(BOOL)autoSnappingEnabled {
	_autoSnappingEnabled = autoSnappingEnabled;
	if (_autoSnappingEnabled) {
		self.scannerViewController.shutterMode = SBSDKShutterModeSmart;
	} else {
		self.scannerViewController.shutterMode = SBSDKShutterModeAlwaysManual;
	}
}

- (void)setSampleSize:(NSInteger)sampleSize {
	_sampleSize = sampleSize;
	self.scannerViewController.imageScale = (CGFloat)(1.0f / (CGFloat)sampleSize);
}

- (void)setAutoSnappingSensitivity:(CGFloat)autoSnappingSensitivity {
	_autoSnappingSensitivity = autoSnappingSensitivity;
	self.scannerViewController.autoCaptureSensitivity = autoSnappingSensitivity;
}

#pragma mark - SBSDKScannerViewControllerDelegate

- (void)scannerControllerWillCaptureStillImage:(SBSDKScannerViewController *)controller {
	if (self.onStartCapturingImage) {
		self.onStartCapturingImage(@{});
	}
}

- (void)scannerController:(SBSDKScannerViewController *)controller
	didFailCapturingImage:(NSError *)error {
	if (!self.onImageCaptureFailed) {
		return;
	}
	self.onImageCaptureFailed(@{@"error":error.localizedDescription});
}

- (BOOL)scannerControllerShouldAnalyseVideoFrame:(SBSDKScannerViewController *)controller {
	// We want to only process video frames when self is visible on screen and front most view controller
	return self.viewAppeared && self.autoSnappingEnabled;
}

- (void)scannerController:(SBSDKScannerViewController *)controller
  didCaptureDocumentImage:(UIImage *)documentImage {
	if (!self.onDocumentImageCaptured) {
		return;
	}
	
	NSString *imageUri = [ImageUtils generateTemporaryDocumentsFilePath:@"jpg"];
	NSURL *imageUrl = [NSURL fileURLWithPath:imageUri];
	
	BOOL result = [ImageUtils saveImage:imageUri
								  image:documentImage
								quality:self.imageCompressionQuality];
	if (result) {
		self.onDocumentImageCaptured(@{@"imageFileUri":imageUrl.absoluteString,
									   @"originalImageFileUri":self.originalImageFileUri,
									   @"polygon":(self.lastDetectedPolygon ? [self.lastDetectedPolygon polygonPoints] : [NSMutableArray new]),
									   @"detectionResult":(self.lastDetectedPolygon ? [self.lastDetectedPolygon detectionStatusString] : @"ERROR_NOTHING_DETECTED")
									   });
		return;
	} else {
		self.onDocumentImageCaptured(@{@"error":@"Image could not be saved."});
	}
}

- (void)scannerController:(SBSDKScannerViewController *)controller
		  didCaptureImage:(nonnull UIImage *)image {
	if (!self.onImageCaptured) {
		return;
	}
	
	NSString *filePathString = [ImageUtils generateTemporaryDocumentsFilePath:@"jpg"];
	self.originalImageFileUri = [NSURL fileURLWithPath:filePathString].absoluteString;
	[ImageUtils saveImage:filePathString
					image:image
				  quality:self.imageCompressionQuality];
	self.onImageCaptured(@{@"imageFileUri":filePathString});
}

- (void)scannerController:(SBSDKScannerViewController *)controller
				 didDetectPolygon:(SBSDKPolygon *)polygon
							 withStatus:(SBSDKDocumentDetectionStatus)status {
	self.lastDetectedPolygon = polygon;
	if (!self.onPolygonDetected) {
		return;
	}
	self.onPolygonDetected(@{@"polygon":[polygon polygonPoints],
							 @"status":[polygon detectionStatusString]});
}

- (NSString *)scannerController:(SBSDKScannerViewController *)controller
localizedTextForDetectionStatus:(SBSDKDocumentDetectionStatus)status {
	if (!self.autoSnappingEnabled) {
		return nil;
	}
	
	JSONOptionsUtils *textResUtils = [[JSONOptionsUtils alloc] initWithOptions:self.textResBundle];
	switch (status) {
		case SBSDKDocumentDetectionStatusOK:
			return [textResUtils stringValueForOption:@"autosnapping_hint_do_not_move"
										 defaultValue:@"Don't move. Capturing document..."];
		case SBSDKDocumentDetectionStatusOK_SmallSize:
			return [textResUtils stringValueForOption:@"autosnapping_hint_move_closer"
										 defaultValue:@"Move closer."];
		case SBSDKDocumentDetectionStatusOK_BadAngles:
			return [textResUtils stringValueForOption:@"autosnapping_hint_bad_angles"
										 defaultValue:@"Turn your device to\nhave a more rectangular outline."];
		case SBSDKDocumentDetectionStatusError_NothingDetected:
			return [textResUtils stringValueForOption:@"autosnapping_hint_nothing_detected"
										 defaultValue:@"Searching for document..."];
		case SBSDKDocumentDetectionStatusError_Noise:
			return [textResUtils stringValueForOption:@"autosnapping_hint_too_noisy"
										 defaultValue:@"Background too noisy!\nSearching for document..."];
		case SBSDKDocumentDetectionStatusError_Brightness:
			return [textResUtils stringValueForOption:@"autosnapping_hint_too_dark"
										 defaultValue:@"Poor light!\nSearching for document..."];
		default:
			return nil;
	}
}

- (UIColor *)scannerController:(SBSDKScannerViewController *)controller
polygonColorForDetectionStatus:(SBSDKDocumentDetectionStatus)status {
	if (!self.edgeColor) {
		return [UIColor redColor];
	}
	return [UIColor colorFromHexString:self.edgeColor];
}


@end
