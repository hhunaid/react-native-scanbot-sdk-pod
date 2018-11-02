//
//  ObjectMapper.h
//  RNScanbotSDK
//
//  Created by Stefan Dragnev on 01.06.18.
//  Copyright © 2018 doo. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ObjectMapper : NSObject

// map of configuration property name -> (map of configuration string -> int value)
+ (void)setEnumerationMapping:(NSDictionary<NSString*, NSDictionary<NSString*, NSNumber*>*>*)enumerationMapping;

+ (void)populateInstance:(id)instance
          fromDictionary:(NSDictionary*)dictionary;

@end
