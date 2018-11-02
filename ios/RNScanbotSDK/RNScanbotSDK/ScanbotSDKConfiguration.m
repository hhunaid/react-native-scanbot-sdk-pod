#import "ScanbotSDKConfiguration.h"

@implementation ScanbotSDKConfiguration
- (instancetype)init {
    self = [super init];
    self.storageImageFormat = SBSDKImageFileFormatJPEG;
    self.storageImageQuality = 80;
    return self;
}
@end
