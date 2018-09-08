//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTComponent.h>
#import <UIKit/UIKit.h>

@interface ScanbotCroppingView : UIView

@property (nonatomic, copy) void (^ _Nullable eventReceivedSignalBlock)(id _Nullable params);

@property (nonatomic) NSString * _Nullable edgeColor;
@property (nonatomic) NSString * _Nullable imageFileUri;
@property (nonatomic) NSNumber * _Nullable imageCompressionQuality;
@property (nonatomic) NSString * _Nullable cancelButtonImageUri;
@property (nonatomic) NSString * _Nullable applyButtonImageUri;
@property (nonatomic, copy) RCTBubblingEventBlock _Nullable onChangesAppliedWithPolygon;
@property (nonatomic, copy) RCTBubblingEventBlock _Nullable onChangesCanceled;

- (void)applyCroppingChanges;
- (void)dismissCroppingChanges;
- (void)rotateImageClockwise;

@end
