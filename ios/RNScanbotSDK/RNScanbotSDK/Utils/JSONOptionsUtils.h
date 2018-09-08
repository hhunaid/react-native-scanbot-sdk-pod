//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreGraphics/CoreGraphics.h>

@interface JSONOptionsUtils : NSObject

- (instancetype)initWithOptions:(NSDictionary *)JSONOptions;

- (BOOL)boolValueForOption:(NSString *)optionName
			  defaultValue:(BOOL)defaultValue;
- (NSInteger)integerValueForOption:(NSString *)optionName
					  defaultValue:(NSInteger)defaultValue;
- (CGFloat)floatValueForOption:(NSString *)optionName
				  defaultValue:(CGFloat)defaultValue;
- (NSString *)stringValueForOption:(NSString *)optionName
					  defaultValue:(NSString *)defaultValue;
- (NSArray *)arrayValueForOption:(NSString *)optionName
					defaultValue:(NSArray *)defaultValue;
- (NSDictionary *)dictionaryValueForOption:(NSString *)optionName
							  defaultValue:(NSDictionary *)defaultValue;

@end
