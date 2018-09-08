//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface OCRUtils : NSObject

+ (NSArray<NSString *> *)checkMissingLanguages:(NSArray<NSString *> *)languages;
+ (NSString *)missingLanguagesStringFromArray:(NSArray<NSString *> *)missingLanguagesArray;

@end
