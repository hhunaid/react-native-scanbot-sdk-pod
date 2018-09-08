//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import <Foundation/Foundation.h>

#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

@interface RNScanbotSDK : NSObject <RCTBridgeModule>

- (void)sbsdk_initializeSDK:(NSString *)licenseKey
             loggingEnabled:(BOOL)loggingEnabled
					success:(RCTResponseSenderBlock)success
					failure:(RCTResponseSenderBlock)failure;
- (void)sbsdk_isLicenseValid:(RCTResponseSenderBlock)success
                     failure:(RCTResponseSenderBlock)failure;
- (void)sbsdk_applyImageFilter:(NSString *)filterType
                  imageFileUri:(NSString *)imageFileUri
            compressionQuality:(NSNumber *)compressionQuality
					   success:(RCTResponseSenderBlock)success
					   failure:(RCTResponseSenderBlock)failure;
- (void)sbsdk_detectDocument:(NSString *)imageUri
          compressionQuality:(NSNumber *)compressionQuality
					 success:(RCTResponseSenderBlock)success
					 failure:(RCTResponseSenderBlock)failure;
- (void)sbsdk_getOCRConfigs:(RCTResponseSenderBlock)success
                    failure:(RCTResponseSenderBlock)failure;
- (void)sbsdk_performOCR:(NSArray *)imageFileUris
               languages:(NSArray *)languages
            outputFormat:(NSString *)outputFormat
				 success:(RCTResponseSenderBlock)success
				 failure:(RCTResponseSenderBlock)failure;
- (void)sbsdk_createPDF:(NSArray *)imageFileUris
				success:(RCTResponseSenderBlock)success
				failure:(RCTResponseSenderBlock)failure;
- (void)sbsdk_rotateImage:(NSString *)imageFileUri
				  degrees:(NSNumber *)degrees
	   compressionQuality:(NSNumber *)compressionQuality
				  success:(RCTResponseSenderBlock)success
				  failure:(RCTResponseSenderBlock)failure;
- (void)sbsdk_cleanup:(RCTResponseSenderBlock)success
			  failure:(RCTResponseSenderBlock)failure;

@end
