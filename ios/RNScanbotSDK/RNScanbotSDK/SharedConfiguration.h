//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import <Foundation/Foundation.h>

static const NSInteger kDefaultImageCompressionQuality = 80;

@interface SharedConfiguration : NSObject

@property (nonatomic) BOOL loggingEnabled;
@property (nonatomic) BOOL isSDKInitialized;

+ (instancetype)defaultConfiguration;

+ (BOOL)isSDKInitialized;

@end
