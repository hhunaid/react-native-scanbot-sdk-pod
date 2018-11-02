#ifndef BarcodeMapping_h
#define BarcodeMapping_h

static inline NSString* stringFromMetadataObjectType(AVMetadataObjectType type) {
    if (type == AVMetadataObjectTypeAztecCode) return @"AZTEC";
    if (type == AVMetadataObjectTypeCode128Code) return @"CODE_128";
    if (type == AVMetadataObjectTypeCode39Code) return @"CODE_39";
    if (type == AVMetadataObjectTypeCode39Mod43Code) return @"CODE_39";
    if (type == AVMetadataObjectTypeCode93Code) return @"CODE_93";
    if (type == AVMetadataObjectTypeDataMatrixCode) return @"DATA_MATRIX";
    if (type == AVMetadataObjectTypeEAN13Code) return @"EAN_13";
    if (type == AVMetadataObjectTypeEAN8Code) return @"EAN_8";
    if (type == AVMetadataObjectTypeITF14Code) return @"ITF";
    if (type == AVMetadataObjectTypePDF417Code) return @"PDF_417";
    if (type == AVMetadataObjectTypeQRCode) return @"QR_CODE";
    if (type == AVMetadataObjectTypeUPCECode) return @"UPC_E";
    return @"UNKNOWN";
}

static inline AVMetadataObjectType metadataObjectTypeFromString(NSString* string) {
    if ([string isEqualToString:@"AZTEC"]) return AVMetadataObjectTypeAztecCode;
    if ([string isEqualToString:@"CODABAR"]) return nil;
    if ([string isEqualToString:@"CODE_128"]) return AVMetadataObjectTypeCode128Code;
    if ([string isEqualToString:@"CODE_39"]) return AVMetadataObjectTypeCode39Code;
    if ([string isEqualToString:@"CODE_93"]) return AVMetadataObjectTypeCode93Code;
    if ([string isEqualToString:@"DATA_MATRIX"]) return AVMetadataObjectTypeDataMatrixCode;
    if ([string isEqualToString:@"EAN_13"]) return AVMetadataObjectTypeEAN13Code;
    if ([string isEqualToString:@"EAN_8"]) return AVMetadataObjectTypeEAN8Code;
    if ([string isEqualToString:@"ITF"]) return AVMetadataObjectTypeInterleaved2of5Code;
    if ([string isEqualToString:@"PDF_417"]) return AVMetadataObjectTypePDF417Code;
    if ([string isEqualToString:@"QR_CODE"]) return AVMetadataObjectTypeQRCode;
    if ([string isEqualToString:@"UPC_A"]) return AVMetadataObjectTypeUPCECode;
    if ([string isEqualToString:@"UPC_E"]) return AVMetadataObjectTypeUPCECode;
    if ([string isEqualToString:@"UPC_EAN_EXTENSION"]) return nil;
    return nil;
}

#endif /* BarcodeMapping_h */
