//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import "JSONOptionsUtils.h"

@interface JSONOptionsUtils ()

@property (nonatomic, strong) NSDictionary *options;

@end

@implementation JSONOptionsUtils

- (instancetype)initWithOptions:(NSDictionary *)JSONOptions {
	if (self = [super init]) {
		self.options = JSONOptions;
	}
	return self;
}

- (BOOL)optionHasKey:(NSString *)key {
	if (self.options) {
		if ([self.options.allKeys containsObject:key]) {
			return YES;
		}
	}
	return NO;
}

- (BOOL)boolValueForOption:(NSString *)optionName
			  defaultValue:(BOOL)defaultValue {
	if ([self optionHasKey:optionName]) {
		if ([self.options[optionName] isKindOfClass:[NSNumber class]]) {
			return [self.options[optionName] boolValue];
		} else {
			return defaultValue;
		}
	}
	return defaultValue;
}

- (NSInteger)integerValueForOption:(NSString *)optionName
					  defaultValue:(NSInteger)defaultValue {
	if ([self optionHasKey:optionName]) {
		if ([self.options[optionName] isKindOfClass:[NSNumber class]]) {
			return [self.options[optionName] integerValue];
		} else {
			return defaultValue;
		}
	}
	return defaultValue;
}

- (CGFloat)floatValueForOption:(NSString *)optionName
				  defaultValue:(CGFloat)defaultValue {
	if ([self optionHasKey:optionName]) {
		if ([self.options[optionName] isKindOfClass:[NSNumber class]]) {
			return [self.options[optionName] floatValue];
		} else {
			return defaultValue;
		}
	}
	return defaultValue;
}

- (NSString *)stringValueForOption:(NSString *)optionName
					  defaultValue:(NSString *)defaultValue {
	if ([self optionHasKey:optionName]) {
		if ([self.options[optionName] isKindOfClass:[NSString class]]) {
			return self.options[optionName];
		} else {
			return defaultValue;
		}
	}
	return defaultValue;
}

- (NSArray *)arrayValueForOption:(NSString *)optionName
					defaultValue:(NSArray *)defaultValue {
	if ([self optionHasKey:optionName]) {
		if ([self.options[optionName] isKindOfClass:[NSArray class]]) {
			return self.options[optionName];
		} else {
			return defaultValue;
		}
	}
	return defaultValue;
}

- (NSDictionary *)dictionaryValueForOption:(NSString *)optionName
							  defaultValue:(NSDictionary *)defaultValue {
	if ([self optionHasKey:optionName]) {
		if ([self.options[optionName] isKindOfClass:[NSDictionary class]]) {
			return self.options[optionName];
		} else {
			return defaultValue;
		}
	}
	return defaultValue;
}

@end
