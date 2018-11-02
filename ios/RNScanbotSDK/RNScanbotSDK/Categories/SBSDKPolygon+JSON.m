//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import "SBSDKPolygon+JSON.h"
#import "JSONMappings.h"

@implementation SBSDKPolygon (JSON)

- (NSArray *)polygonPoints {
    NSMutableArray *points = [NSMutableArray array];
    for (NSInteger index = 0; index < 4; index++) {
        CGPoint point = [self normalizedPointWithIndex:index];
        [points addObject:@{@"x":@(point.x),
                            @"y":@(point.y)}];
    }
    return [NSArray arrayWithArray:points];
}

- (NSString *)detectionStatusString {
    return DetectionResultAsJSONStringValue(self.detectionStatus);
}

@end
