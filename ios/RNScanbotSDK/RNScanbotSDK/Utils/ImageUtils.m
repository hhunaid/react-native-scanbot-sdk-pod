//
//  Scanbot SDK ReactNative Module
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import "ImageUtils.h"

@implementation ImageUtils

+ (NSString *)tempDirectoryPath {
    NSString *docsPath = [[ImageUtils appDocumentsDirectory] path];
    return [NSString stringWithFormat:@"%@/%@", docsPath, sbsdkTmpFolder];
}

+ (void)recreateTempDirectoryIfNeeded {
    NSString *sbsdkTempPath = [ImageUtils tempDirectoryPath];
    if (![[NSFileManager defaultManager] fileExistsAtPath:sbsdkTempPath]) {
        [[NSFileManager defaultManager] createDirectoryAtPath:sbsdkTempPath
                                  withIntermediateDirectories:NO
                                                   attributes:nil
                                                        error:nil];
    }
}

+ (NSURL *)appDocumentsDirectory {
    return [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject];
}

+ (NSString *)generateTemporaryFileName:(NSString *)extension {
    NSUUID *UUID = [NSUUID UUID];
    NSString *stringUUID = [UUID UUIDString];
    return [NSString stringWithFormat:@"%@.%@", stringUUID, extension];
}

+ (NSString *)generateTemporaryDocumentsFilePath:(NSString *)extension {
    NSString *sbsdkTempPath = [ImageUtils tempDirectoryPath];
    return [NSString stringWithFormat:@"%@/%@", sbsdkTempPath, [self generateTemporaryFileName:extension]];
}

+ (UIImage *)loadImage:(NSString *)imageFilePath {
    if ([ImageUtils imageFileExists:imageFilePath]) {
        UIImage *image = [UIImage imageWithData:[NSData dataWithContentsOfFile:imageFilePath]];
        if (!image) {
            image = [UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:imageFilePath]]];
        }
        return image;
    } else {
        return nil;
    }
}

+ (BOOL)saveImage:(NSString *)imageFilePath
			image:(UIImage *)image
		  quality:(NSInteger)quality {
    [ImageUtils recreateTempDirectoryIfNeeded];
    NSData *imageData = UIImageJPEGRepresentation(image, ((CGFloat)quality / 100.0));
    return [imageData writeToFile:imageFilePath atomically:YES];
}

+ (NSError *)removeAllFilesFromTemporaryDocumentsDirectory {
    NSString *sbsdkTempPath = [ImageUtils tempDirectoryPath];
    NSFileManager *fileManager = [NSFileManager new];
    NSDirectoryEnumerator *enumerator = [fileManager enumeratorAtPath:sbsdkTempPath];
    NSError *err = nil;
    BOOL result;
    
    NSString *fileName;
    while (fileName = [enumerator nextObject]) {
        result = [fileManager removeItemAtPath:[sbsdkTempPath stringByAppendingPathComponent:fileName] error:&err];
        if (!result && err) {
            break;
        }
    }
    return err;
}

+ (BOOL)imageFileExists:(NSString *)imageFileUri {
    return [[NSFileManager defaultManager] fileExistsAtPath:[NSURL URLWithString:imageFileUri].path];
}

+ (SBSDKIndexedImageStorage *)imageStorageFromFilesList:(NSArray <NSString *> *)imageFilePaths {
    NSString *tempSubFolderName = [[NSUUID UUID] UUIDString];
    NSURL *tempStorageDirUrl = [[NSURL alloc] initFileURLWithPath: [NSString stringWithFormat:@"%@/%@", ImageUtils.tempDirectoryPath, tempSubFolderName]];
    SBSDKStorageLocation *location = [[SBSDKStorageLocation alloc] initWithBaseURL:tempStorageDirUrl];
    SBSDKIndexedImageStorage *storage = [[SBSDKIndexedImageStorage alloc] initWithStorageLocation:location];
    for (NSString *imageFilePath in imageFilePaths) {
        UIImage *image = [ImageUtils loadImage:imageFilePath];
        if (image) {
            [storage addImage:image];
        }
    }
    return storage;
}

@end
