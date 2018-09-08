//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import "OCRUtils.h"
@import ScanbotSDK;

@implementation OCRUtils

+ (NSArray<NSString *> *)checkMissingLanguages:(NSArray<NSString *> *)languages {
  NSArray *installedLanguages = [SBSDKOpticalTextRecognizer installedLanguages];
  NSMutableArray<NSString *> *missingLanguges = [NSMutableArray array];
  for (NSString *languageCode in languages) {
    if (![installedLanguages containsObject:languageCode]) {
      [missingLanguges addObject:languageCode];
    }
  }
  return missingLanguges;
}

+ (NSString *)missingLanguagesStringFromArray:(NSArray<NSString *> *)missingLanguagesArray {
  NSString *arrayRepresentation = @"";
  for (NSInteger index = 0; index < missingLanguagesArray.count; index++) {
    NSString *languageCode = missingLanguagesArray[index];
    arrayRepresentation = [arrayRepresentation stringByAppendingFormat:@"\"%@\"", languageCode];
    if (index < missingLanguagesArray.count - 1) {
      arrayRepresentation = [arrayRepresentation stringByAppendingString:@", "];
    }
  }
  return [NSString stringWithFormat:@"Missing OCR language files for languages: [%@]", arrayRepresentation];
}

@end
