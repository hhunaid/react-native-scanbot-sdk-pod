#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

@import ScanbotSDK;

@interface DefaultUILauncher : NSObject <RCTBridgeModule>
@end

@interface DocumentScannerPromiseProxy : NSObject <SBSDKUIDocumentScannerViewControllerDelegate>
- init NS_UNAVAILABLE;
- initWithResolver:(nonnull RCTPromiseResolveBlock)resolve NS_DESIGNATED_INITIALIZER;
@end

@interface CroppingScreenPromiseProxy : NSObject <SBSDKUICroppingViewControllerDelegate>
- init NS_UNAVAILABLE;
- initWithResolver:(nonnull RCTPromiseResolveBlock)resolve NS_DESIGNATED_INITIALIZER;
@end

@interface MRZScannerPromiseProxy : NSObject <SBSDKUIMRZScannerViewControllerDelegate>
- init NS_UNAVAILABLE;
- initWithResolver:(nonnull RCTPromiseResolveBlock)resolve NS_DESIGNATED_INITIALIZER;
@end

@interface BarcodeScannerPromiseProxy : NSObject <SBSDKUIBarcodeScannerViewControllerDelegate>
- init NS_UNAVAILABLE;
- initWithResolver:(nonnull RCTPromiseResolveBlock)resolve NS_DESIGNATED_INITIALIZER;
@end
