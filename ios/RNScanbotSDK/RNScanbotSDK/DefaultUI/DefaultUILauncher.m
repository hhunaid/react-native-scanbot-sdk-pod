#import "DefaultUILauncher.h"
#import "ObjectMapper.h"
#import "SBSDKPolygon+JSON.h"
#import "JSONMappings.h"
#import "ImageUtils.h"
#import "RNScanbotSDK.h"
#import "BarcodeMapping.h"

@import ScanbotSDK;

@implementation DefaultUILauncher

RCT_EXPORT_MODULE(SBSDKDefaultUi);

// Necessary so that the start* methods get called on the UI thread.
- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(startDocumentScanner:(NSDictionary*)configuration
                           withResolver:(RCTPromiseResolveBlock)resolve
                           withRejecter:(RCTPromiseRejectBlock)reject) {
    
    SBSDKUIDocumentScannerUIConfiguration* uiConfig = [[SBSDKUIDocumentScannerUIConfiguration alloc] init];
    SBSDKUIDocumentScannerBehaviorConfiguration* behaviorConfig = [[SBSDKUIDocumentScannerBehaviorConfiguration alloc] init];
    SBSDKUIDocumentScannerTextConfiguration* textConfig = [[SBSDKUIDocumentScannerTextConfiguration alloc] init];
    
    @try {
        [ObjectMapper populateInstance:uiConfig fromDictionary:configuration];
        [ObjectMapper populateInstance:behaviorConfig fromDictionary:configuration];
        [ObjectMapper populateInstance:textConfig fromDictionary:configuration];
    }
    @catch (NSException* ex) {
        reject(@"Property error", [ex reason], nil);
        return;
    }
    
    SBSDKUIDocumentScannerConfiguration* config = [[SBSDKUIDocumentScannerConfiguration alloc] initWithUIConfiguration:uiConfig textConfiguration:textConfig behaviorConfiguration:behaviorConfig];
    
    DocumentScannerPromiseProxy* delegate = [[DocumentScannerPromiseProxy alloc] initWithResolver:resolve];
    
    SBSDKUIDocumentScannerViewController* viewController = [SBSDKUIDocumentScannerViewController createNewWithDocument:nil configuration:config andDelegate:delegate];
    UIViewController* rootViewController = [UIApplication sharedApplication].delegate.window.rootViewController;
    [rootViewController presentViewController:viewController animated:true completion:nil];
}

RCT_EXPORT_METHOD(startCroppingScreen:(NSDictionary*)page
                  configuration:(NSDictionary*)configuration
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject) {
    
    SBSDKUICroppingScreenTextConfiguration* textConfig = [[SBSDKUICroppingScreenTextConfiguration alloc] init];
    SBSDKUICroppingScreenUIConfiguration* uiConfig = [[SBSDKUICroppingScreenUIConfiguration alloc] init];
    
    SBSDKUICroppingScreenConfiguration* config = [[SBSDKUICroppingScreenConfiguration alloc] initWithUIConfiguration:uiConfig textConfiguration:textConfig];
    
    CroppingScreenPromiseProxy* delegate = [[CroppingScreenPromiseProxy alloc] initWithResolver:resolve];
    
    SBSDKUICroppingViewController* viewController = [SBSDKUICroppingViewController createNewWithPage:pageFromDictionary(page) withConfiguration:config andDelegate:delegate];
    UIViewController* rootViewController = [UIApplication sharedApplication].delegate.window.rootViewController;
    [rootViewController presentViewController:viewController animated:true completion:nil];
}

RCT_EXPORT_METHOD(startMrzScanner:(NSDictionary*)configuration
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject) {
    
    SBSDKUIMachineCodeScannerUIConfiguration* uiConfig = [[SBSDKUIMachineCodeScannerUIConfiguration alloc] init];
    SBSDKUIMachineCodeScannerBehaviorConfiguration* behaviorConfig = [[SBSDKUIMachineCodeScannerBehaviorConfiguration alloc] init];
    SBSDKUIMachineCodeScannerTextConfiguration* textConfig = [[SBSDKUIMachineCodeScannerTextConfiguration alloc] init];
    
    @try {
        [ObjectMapper populateInstance:uiConfig fromDictionary:configuration];
        [ObjectMapper populateInstance:behaviorConfig fromDictionary:configuration];
        [ObjectMapper populateInstance:textConfig fromDictionary:configuration];
    }
    @catch (NSException* ex) {
        reject(@"Property error", [ex reason], nil);
        return;
    }
    
    SBSDKUIMachineCodeScannerConfiguration* config = [[SBSDKUIMachineCodeScannerConfiguration alloc] initWithUIConfiguration:uiConfig textConfiguration:textConfig behaviorConfiguration:behaviorConfig];
    
    MRZScannerPromiseProxy* delegate = [[MRZScannerPromiseProxy alloc] initWithResolver:resolve];
    
    SBSDKUIMRZScannerViewController* viewController = [SBSDKUIMRZScannerViewController createNewWithConfiguration:config andDelegate:delegate];
    UIViewController* rootViewController = [UIApplication sharedApplication].delegate.window.rootViewController;
    [rootViewController presentViewController:viewController animated:true completion:nil];
}

RCT_EXPORT_METHOD(startBarcodeScanner:(NSDictionary*)configuration
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject) {
    
    SBSDKUIMachineCodeScannerUIConfiguration* uiConfig = [[SBSDKUIMachineCodeScannerUIConfiguration alloc] init];
    SBSDKUIMachineCodeScannerBehaviorConfiguration* behaviorConfig = [[SBSDKUIMachineCodeScannerBehaviorConfiguration alloc] init];
    SBSDKUIMachineCodeScannerTextConfiguration* textConfig = [[SBSDKUIMachineCodeScannerTextConfiguration alloc] init];
    
    @try {
        [ObjectMapper populateInstance:uiConfig fromDictionary:configuration];
        [ObjectMapper populateInstance:behaviorConfig fromDictionary:configuration];
        [ObjectMapper populateInstance:textConfig fromDictionary:configuration];
    }
    @catch (NSException* ex) {
        reject(@"Property error", [ex reason], nil);
        return;
    }
    
    NSArray* barcodeFormats = [configuration objectForKey:@"barcodeFormats"];
    NSMutableArray* machineCodeTypes = nil;
    if (barcodeFormats && [barcodeFormats indexOfObject:@"ALL_FORMATS"] == NSNotFound) {
        machineCodeTypes = [NSMutableArray array];
        for (NSString* format in barcodeFormats) {
            AVMetadataObjectType type = metadataObjectTypeFromString(format);
            if (type) {
                [machineCodeTypes addObject:type];
            }
        }
    }
    
    SBSDKUIMachineCodeScannerConfiguration* config = [[SBSDKUIMachineCodeScannerConfiguration alloc] initWithUIConfiguration:uiConfig textConfiguration:textConfig behaviorConfiguration:behaviorConfig];
    
    BarcodeScannerPromiseProxy* delegate = [[BarcodeScannerPromiseProxy alloc] initWithResolver:resolve];
    
    SBSDKUIBarcodeScannerViewController* viewController = [SBSDKUIBarcodeScannerViewController createNewWithAcceptedMachineCodeTypes:machineCodeTypes configuration:config andDelegate:delegate];
    UIViewController* rootViewController = [UIApplication sharedApplication].delegate.window.rootViewController;
    [rootViewController presentViewController:viewController animated:true completion:nil];
}

@end

@implementation DocumentScannerPromiseProxy {
    RCTPromiseResolveBlock _resolve;

    // This field retains a reference to the object until one of the callbacks gets called
    DocumentScannerPromiseProxy* _strongSelf;
}

- initWithResolver:(nonnull RCTPromiseResolveBlock)resolve {
    _resolve = resolve;
    _strongSelf = self;
    return [super init];
}

- (void)scanningViewController:(nonnull SBSDKUIDocumentScannerViewController *)viewController
         didFinishWithDocument:(nonnull SBSDKUIDocument *)document {
    NSLog(@"scanningViewController:(nonnull SBSDKUIDocumentScannerViewController *)viewController didScanDocumentPages");
    
    NSMutableArray* result = [NSMutableArray new];
    
    for (int i = 0; i < [document numberOfPages]; ++i) {
        [result addObject:dictionaryFromPage([document pageAtIndex:i])];
    }
   
    _resolve(@{
               @"status": [result count] > 0 ? @"OK" : @"CANCELED",
               @"pages": result
               });
    
    // release instance
    _strongSelf = nil;
}

- (void)scanningViewControllerDidCancel:(SBSDKUIDocumentScannerViewController *)viewController {
    NSLog(@"scanningViewControllerDidDismiss:(SBSDKUIDocumentScannerViewController *)viewController");
    _resolve(@{@"status": @"CANCELED", @"pages": @[]});

    // release instance
    _strongSelf = nil;
}

@end

@implementation CroppingScreenPromiseProxy {
    RCTPromiseResolveBlock _resolve;
    CroppingScreenPromiseProxy* _strongSelf;
}

- initWithResolver:(nonnull RCTPromiseResolveBlock)resolve {
    _resolve = resolve;
    _strongSelf = self;
    return [super init];
}

- (void)croppingViewController:(SBSDKUICroppingViewController *)viewController didFinish:(SBSDKUIPage *)changedPage {
    _resolve(@{
               @"status": @"OK",
               @"page": dictionaryFromPage(changedPage)
               });

    // release instance
    _strongSelf = nil;
}

- (void)croppingViewControllerDidCancel:(SBSDKUICroppingViewController *)viewController {
    _resolve(@{@"status": @"CANCELED"});
    
    // release instance
    _strongSelf = nil;
}

@end

@implementation MRZScannerPromiseProxy {
    RCTPromiseResolveBlock _resolve;
    MRZScannerPromiseProxy* _strongSelf;
}

- initWithResolver:(nonnull RCTPromiseResolveBlock)resolve {
    self = [super init];
    _resolve = resolve;
    _strongSelf = self;
    return self;
}

- (void)mrzDetectionViewController:(SBSDKUIMRZScannerViewController *)viewController didDetect:(SBSDKMachineReadableZoneRecognizerResult *)zone {
    NSDictionary* result = MRZRecognizerResultAsDictionary(zone);
    [result setValue:@"OK" forKey:@"status"];
    _resolve(result);

    [viewController dismissViewControllerAnimated:TRUE completion:nil];

    // release instance
    _strongSelf = nil;
}

- (void)mrzDetectionViewControllerDidCancel:(SBSDKUIMRZScannerViewController *)viewController {
    _resolve(@{@"status": @"CANCELED"});
    
    // release instance
    _strongSelf = nil;
}

@end

@implementation BarcodeScannerPromiseProxy {
    RCTPromiseResolveBlock _resolve;
    BarcodeScannerPromiseProxy* _strongSelf;
}

- initWithResolver:(nonnull RCTPromiseResolveBlock)resolve {
    _resolve = resolve;
    _strongSelf = self;
    return [super init];
}

- (void)qrBarcodeDetectionViewController:(SBSDKUIBarcodeScannerViewController *)viewController didDetect:(SBSDKMachineReadableCode *)code {
    _resolve(@{
               @"status": @"OK",
               @"value": [code stringValue],
               @"format": stringFromMetadataObjectType(code.metadata.codeObject.type),
               });

    [viewController dismissViewControllerAnimated:TRUE completion:nil];

    // release instance
    _strongSelf = nil;
}

- (void)qrBarcodeDetectionViewControllerDidCancel:(SBSDKUIBarcodeScannerViewController *)viewController {
    _resolve(@{@"status": @"CANCELED"});
    
    // release instance
    _strongSelf = nil;
}

@end
