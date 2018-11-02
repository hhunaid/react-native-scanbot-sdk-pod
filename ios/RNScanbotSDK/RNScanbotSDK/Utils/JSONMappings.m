@import ScanbotSDK;

#import <Foundation/Foundation.h>
#import "SBSDKPolygon+JSON.h"
#import "HashUtils.h"

NSString * DetectionResultAsJSONStringValue(SBSDKDocumentDetectionStatus status) {
    switch (status) {
        case SBSDKDocumentDetectionStatusOK:
            return @"OK";
        case SBSDKDocumentDetectionStatusOK_SmallSize:
            return @"OK_BUT_TOO_SMALL";
        case SBSDKDocumentDetectionStatusOK_BadAngles:
            return @"OK_BUT_BAD_ANGLES";
        case SBSDKDocumentDetectionStatusOK_BadAspectRatio:
            return @"OK_BUT_BAD_ASPECT_RATIO";
        case SBSDKDocumentDetectionStatusError_NothingDetected:
            return @"ERROR_NOTHING_DETECTED";
        case SBSDKDocumentDetectionStatusError_Brightness:
            return @"ERROR_TOO_DARK";
        case SBSDKDocumentDetectionStatusError_Noise:
            return @"ERROR_TOO_NOISY";
        default:
            return @"";
    }
}

NSString* ImageFilterAsJSONStringValue(SBSDKImageFilterType filter) {
    switch (filter) {
        case SBSDKImageFilterTypeNone: return @"NONE";
        case SBSDKImageFilterTypeColor: return @"COLOR_ENHANCED";
        case SBSDKImageFilterTypeGray: return @"GRAYSCALE";
        case SBSDKImageFilterTypeBinarized: return @"BINARIZED";
        case SBSDKImageFilterTypeColorDocument: return @"COLOR_DOCUMENT";
        case SBSDKImageFilterTypePureBinarized: return @"PURE_BINARIZED";
        case SBSDKImageFilterTypeBackgroundClean: return @"BACKGROUND_CLEAN";
        case SBSDKImageFilterTypeBlackAndWhite: return @"BLACK_AND_WHITE";
        default: return @"";
    }
}

SBSDKImageFilterType filterIdFromFilterNameString(NSString* filterName) {
    if (!filterName) {
        return SBSDKImageFilterTypeNone;
    }
    
    if ([filterName isEqualToString:@"COLOR_ENHANCED"]) return SBSDKImageFilterTypeColor;
    if ([filterName isEqualToString:@"GRAYSCALE"]) return SBSDKImageFilterTypeGray;
    if ([filterName isEqualToString:@"BINARIZED"]) return SBSDKImageFilterTypeBinarized;
    if ([filterName isEqualToString:@"COLOR_DOCUMENT"]) return SBSDKImageFilterTypeColorDocument;
    if ([filterName isEqualToString:@"PURE_BINARIZED"]) return SBSDKImageFilterTypePureBinarized;
    if ([filterName isEqualToString:@"BACKGROUND_CLEAN"]) return SBSDKImageFilterTypeBackgroundClean;
    if ([filterName isEqualToString:@"BLACK_AND_WHITE"]) return SBSDKImageFilterTypeBlackAndWhite;
    return SBSDKImageFilterTypeNone;
}

NSString* MRZFieldNameAsString(SBSDKMachineReadableZoneRecognizerFieldName name) {
    switch (name) {
        case SBSDKMachineReadableZoneRecognizerFieldNameUnknown:
            return @"Unknown";
        case SBSDKMachineReadableZoneRecognizerFieldNameDocumentCode:
            return @"DocumentCode";
        case SBSDKMachineReadableZoneRecognizerFieldNameIssuingStateOrOrganization:
            return @"IssuingStateOrOrganization";
        case SBSDKMachineReadableZoneRecognizerFieldNameDepartmentOfIssuance:
            return @"DepartmentOfIssuance";
        case SBSDKMachineReadableZoneRecognizerFieldNameFirstName:
            return @"FirstName";
        case SBSDKMachineReadableZoneRecognizerFieldNameLastName:
            return @"LastName";
        case SBSDKMachineReadableZoneRecognizerFieldNameNationality:
            return @"Nationality";
        case SBSDKMachineReadableZoneRecognizerFieldNameDateOfBirth:
            return @"DateOfBirth";
        case SBSDKMachineReadableZoneRecognizerFieldNameGender:
            return @"Gender";
        case SBSDKMachineReadableZoneRecognizerFieldNameDateOfExpiry:
            return @"DateOfExpiry";
        case SBSDKMachineReadableZoneRecognizerFieldNamePersonalNumber:
            return @"PersonalNumber";
        case SBSDKMachineReadableZoneRecognizerFieldNameTravelDocumentType:
            return @"TravelDocumentType";
        case SBSDKMachineReadableZoneRecognizerFieldNameOptional1:
            return @"Optional1";
        case SBSDKMachineReadableZoneRecognizerFieldNameOptional2:
            return @"Optional2";
        case SBSDKMachineReadableZoneRecognizerFieldNameDiscreetIssuingStateOrOrganization:
            return @"DiscreetIssuingStateOrOrganization";
        default:
            return @"";
    }
}

NSDictionary* dictionaryFromPage(SBSDKUIPage* page) {
    NSMutableDictionary* result = @{
                                    @"pageId": [page.pageFileUUID UUIDString],
                                    @"polygon": (page.polygon ? [page.polygon polygonPoints] : [NSArray new]),
                                    @"filter": ImageFilterAsJSONStringValue(page.filter),
                                    @"detectionResult": DetectionResultAsJSONStringValue(page.status),
                                    @"originalImageFileUri": uriWithMinihash(page.originalImageURL),
                                    @"originalPreviewImageFileUri": uriWithMinihash([SBSDKUIPageFileStorage.defaultStorage previewImageURLWithPageFileID:page.pageFileUUID pageFileType:SBSDKUIPageFileTypeOriginal]),
                                    }.mutableCopy;
    
    NSURL* documentImageUrl = page.documentImageURL;
    if (documentImageUrl) {
        NSURL* documentPreviewImageUrl = [SBSDKUIPageFileStorage.defaultStorage previewImageURLWithPageFileID:page.pageFileUUID pageFileType:SBSDKUIPageFileTypeDocument];
        [result setObject:uriWithMinihash(documentImageUrl) forKey:@"documentImageFileUri"];
        [result setObject:uriWithMinihash(documentPreviewImageUrl) forKey:@"documentPreviewImageFileUri"];
    }
    
    return result;
}

CGPoint CGPointFromNSArray(NSDictionary<NSString*, NSNumber*>* pt) {
    CGFloat x = [pt[@"x"] doubleValue];
    CGFloat y = [pt[@"y"] doubleValue];
    
    return CGPointMake(x, y);
}

SBSDKUIPage* pageFromDictionary(NSDictionary* dict) {
    SBSDKPolygon* poly = nil;
    NSArray* points = [dict objectForKey:@"polygon"];
    if (points != nil && [points count] == 4) {
        poly = [[SBSDKPolygon alloc] initWithNormalizedPointA:CGPointFromNSArray(points[0])
                                                       pointB:CGPointFromNSArray(points[1])
                                                       pointC:CGPointFromNSArray(points[2])
                                                       pointD:CGPointFromNSArray(points[3])];
    }
    
    NSUUID* uuid = [[NSUUID alloc] initWithUUIDString:dict[@"pageId"]];
    
    return [[SBSDKUIPage alloc] initWithPageFileID:uuid polygon:poly filter:filterIdFromFilterNameString([dict objectForKey:@"filter"])];
}

NSString* MRZDocumentTypeAsString(SBSDKMachineReadableZoneRecognizerResultDocumentType documentType) {
    switch (documentType) {
        case SBSDKMachineReadableZoneRecognizerResultDocumentTypePassport:
            return @"PASSPORT";
        case SBSDKMachineReadableZoneRecognizerResultDocumentTypeTravelDocument:
            return @"TRAVEL_DOCUMENT";
        case SBSDKMachineReadableZoneRecognizerResultDocumentTypeVisa:
            return @"VISA";
        case SBSDKMachineReadableZoneRecognizerResultDocumentTypeIDCard:
            return @"ID_CARD";
        case SBSDKMachineReadableZoneRecognizerResultDocumentTypeUndefined:
            return @"UNDEFINED";
        default:
            return @"UNDEFINED";
    }
}

NSDictionary* MRZRecognizerResultAsDictionary(SBSDKMachineReadableZoneRecognizerResult* mrzResult) {
    NSMutableArray* fields = [NSMutableArray array];
    for (SBSDKMachineReadableZoneRecognizerField* field in mrzResult.fields) {
        [fields addObject:@{@"name": MRZFieldNameAsString(field.fieldName),
                            @"value": field.value,
                            @"confidence": @(field.averageRecognitionConfidence)
                            }];
    }

    NSMutableDictionary* result = @{
                                    @"recognitionSuccessful": @(mrzResult.recognitionSuccessfull),
                                    @"documentType": MRZDocumentTypeAsString(mrzResult.documentType),
                                    @"checkDigitsCount": @(mrzResult.checkDigitsCount),
                                    @"validCheckDigitsCount": @(mrzResult.validCheckDigitsCount),
                                    @"fields": fields
                                    }.mutableCopy;

    return result;
}
