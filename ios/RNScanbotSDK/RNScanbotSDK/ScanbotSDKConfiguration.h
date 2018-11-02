#import <Foundation/Foundation.h>

@import ScanbotSDK;

@interface ScanbotSDKConfiguration : NSObject
@property(nonatomic) BOOL loggingEnabled;
@property(nonatomic) NSString* licenseKey;
@property(nonatomic) int storageImageQuality;
@property(nonatomic) SBSDKImageFileFormat storageImageFormat;

- (instancetype)init;
@end
