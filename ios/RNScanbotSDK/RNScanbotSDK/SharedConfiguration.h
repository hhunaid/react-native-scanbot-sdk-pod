//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ScanbotSDKConfiguration.h"

@interface SharedConfiguration : NSObject

@property (nonatomic) ScanbotSDKConfiguration* sdkConfiguration;
@property (nonatomic) BOOL isSDKInitialized;

+ (instancetype)defaultConfiguration;
+ (BOOL)isSDKInitialized;

@end
