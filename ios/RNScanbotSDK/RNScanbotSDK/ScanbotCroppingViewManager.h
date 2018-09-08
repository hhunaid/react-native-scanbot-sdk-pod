//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import <Foundation/Foundation.h>

#if __has_include(<React/RCTBridge.h>)
#import <React/RCTViewManager.h>
#import <React/RCTConvert.h>
#else
#import "RCTViewManager.h"
#import "RCTConvert.h"
#endif

#import "ScanbotCroppingView.h"

@interface ScanbotCroppingViewManager : RCTViewManager

+ (ScanbotCroppingView *)croppingView;

@end
