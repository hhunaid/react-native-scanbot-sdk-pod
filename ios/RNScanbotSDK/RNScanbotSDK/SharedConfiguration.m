//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import "SharedConfiguration.h"

@implementation SharedConfiguration

+ (instancetype)defaultConfiguration {
    static SharedConfiguration *config = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        config = [self new];
    });
    return config;
}

+ (BOOL)isSDKInitialized {
  return [SharedConfiguration defaultConfiguration].isSDKInitialized;
}

@end
