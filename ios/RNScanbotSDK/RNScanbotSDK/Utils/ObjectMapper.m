//
//  ObjectMapper.m
//  RNScanbotSDK
//
//  Created by Stefan Dragnev on 01.06.18.
//  Copyright © 2018 doo. All rights reserved.
//

#import "ObjectMapper.h"
#import <objc/runtime.h>
#import "UIColor+JSON.h"

static NSDictionary<NSString*, NSDictionary<NSString*, NSNumber*>*>* _enumerationMapping;

@implementation ObjectMapper

+ (void)setEnumerationMapping:(NSDictionary<NSString*, NSDictionary<NSString*, NSNumber*>*>*)enumerationMapping {
    _enumerationMapping = enumerationMapping;
}

+ (void)populateInstance:(id)instance
          fromDictionary:(NSDictionary*)dictionary {
    Class cls = [instance class];
    
    uint outCount;
    objc_property_t* properties = class_copyPropertyList(cls, &outCount);
    
    for (uint pi = 0; pi < outCount; ++pi) {
        const char *name = property_getName(properties[pi]);
        NSString* key = [NSString stringWithUTF8String:name];
        
        id value = [dictionary objectForKey:key];

        // normalize OK and Ok
        if (!value && [key hasSuffix:@"Ok"]) {
            NSString* okKey = [key stringByReplacingOccurrencesOfString:@"Ok" withString:@"OK"];
            value = [dictionary objectForKey:okKey];
        }

        // fix flashImageButtonHidden to flashButtonHidden
        if (!value && [key isEqualToString:@"flashImageButtonHidden"]) {
            value = [dictionary objectForKey:@"flashButtonHidden"];
        }

        if (value) {
            NSDictionary<NSString*, NSNumber*>* valuesDict = [_enumerationMapping objectForKey:key];
            if (valuesDict) {
                value = [valuesDict objectForKey:value];
                if (!value) {
                    @throw [NSException exceptionWithName:@"ArgumentException" reason:[NSString stringWithFormat:@"Invalid enumeration value for parameter %@", key] userInfo:nil];
                }
            } else if ([key containsString:@"Color"]) {
                value = [UIColor colorFromHexString:value];
            }
            
            [instance setValue:value forKey:key];
        }
    }
    
    free(properties);
}

@end
