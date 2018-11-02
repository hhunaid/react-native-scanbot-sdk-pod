export interface DocumentScannerConfiguration
{
    /**
    * The minimum score in percent (0 - 100) of the perspective distortion to accept a detected document.
    * Default is 75.0.
    */
    acceptedAngleScore?: number;
    /**
    * The minimum document width or height in percent (0 - 100) of the screen size to accept a detected document.
    * Default is 80.0.
    */
    acceptedSizeScore?: number;
    /**
    * Controls whether the auto-snapping toggle button is hidden or not.
    */
    autoSnappingButtonHidden?: boolean;
    /**
    * Title of the auto-snapping toggle button.
    */
    autoSnappingButtonTitle?: string;
    /**
    * When auto-snapping is enabled the document scanner will take a photo automatically
    * when a document is detected, conditions are good and the auto-snapping time-out elapses. In this
    * mode the user can still tap the shutter button to snap a document.
    */
    autoSnappingEnabled?: boolean;
    /**
    * Controls the auto-snapping speed. Sensitivity must be within the 0..1 range.
    * A value of 1.0 triggers automatic capturing immediately, a value of 0.0 delays the automatic by 3 seconds.
    * The default value is 0.66 (2 seconds)
    */
    autoSnappingSensitivity?: number;
    /**
    * The background color of the bottom shutter-bar.
    */
    bottomBarBackgroundColor?: string;
    /**
    * The color of the title of all buttons in the bottom shutter-bar (Cancel button, etc.),
    * as well as the camera permission prompt button.
    */
    bottomBarButtonsColor?: string;
    /**
    * The color of the camera background (relevant only when the camera preview mode is CameraPreviewMode.FIT_IN).
    */
    cameraBackgroundColor?: string;
    /**
    * Preview mode of the camera: Fit-In or Fill-In.
    * Optional, default is Fit-In.
    */
    cameraPreviewMode?: CameraPreviewMode;
    /**
    * Title of the cancel button.
    */
    cancelButtonTitle?: string;
    /**
    * Title of the button that opens the screen where the user can allow
    * the usage of the camera by the app.
    */
    enableCameraButtonTitle?: string;
    /**
    * Text that will be displayed when the app
    * is not allowed to use the camera, prompting the user
    * to enable the usage of the camera.
    */
    enableCameraExplanationText?: string;
    /**
    * Controls whether the flash toggle button is hidden or not.
    */
    flashButtonHidden?: boolean;
    /**
    * Title of the flash toggle button.
    */
    flashButtonTitle?: string;
    /**
    * Controls whether the flash should be initially enabled.
    * The default value is FALSE.
    */
    flashEnabled?: boolean;
    flashImageButtonHidden?: boolean;
    /**
    * Sets whether to ignore the net.doo.snap.lib.detector.DetectionResult.OK_BUT_BAD_ASPECT_RATIO detection status.
    * By default BadAspectRatio is not ignored.
    */
    ignoreBadAspectRatio?: boolean;
    /**
    * The image scaling factor. The factor must be within the 0..1 range.
    * A factor of 1 means that the resulting images retain their original size.
    * When the factor is less than 1, resulting images will be made smaller by that factor.
    * By default the scale is 1.
    */
    imageScale?: number;
    /**
    * Controls whether the multi-page toggle button is hidden or not.
    */
    multiPageButtonHidden?: boolean;
    /**
    * Title of the multi-page mode toggle button.
    */
    multiPageButtonTitle?: string;
    /**
    * Controls multi-page mode. When enabled, the user can take multiple document photos before
    * closing the screen by tapping the page counter button. When disabled, the screen will be
    * closed immediately after the first document photo is made.
    * The default value is FALSE.
    */
    multiPageEnabled?: boolean;
    /**
    * Orientation lock mode of the camera: PORTRAIT or LANDSCAPE.
    * By default the camera orientation is not locked.
    */
    orientationLockMode?: CameraOrientationMode;
    /**
    * Title suffix of the button that finishes the document scanning when multi-page scanning is enabled.
    * The button's title has the format "# Pages", where # shows the number of images captured up to now and the
    * suffix "Pages" is set using this method.
    */
    pageCounterButtonTitle?: string;
    /**
    * The background color of the detected document outline when the document's angle, size or aspect ratio
    * is not yet sufficiently good.
    * (All net.doo.snap.lib.detector.DetectionResult with OK_BUT_XXX).
    */
    polygonBackgroundColor?: string;
    /**
    * The background color of the detected document outline when we are ready to snap net.doo.snap.lib.detector.DetectionResult.OK.
    */
    polygonBackgroundColorOK?: string;
    /**
    * The color of the detected document outline when the document's angle, size or aspect ratio
    * is not yet sufficiently good.
    * (All detection statuses in net.doo.snap.lib.detector.DetectionResult that have the OK_BUT_XXX prefix).
    */
    polygonColor?: string;
    /**
    * The color of the detected document outline when we are ready to snap net.doo.snap.lib.detector.DetectionResult.OK.
    */
    polygonColorOK?: string;
    /**
    * Width of the detected document outline.
    */
    polygonLineWidth?: number;
    /**
    * The foreground color of the shutter button in auto-snapping mode.
    */
    shutterButtonAutoInnerColor?: string;
    /**
    * The background color of the shutter button in auto-snapping mode.
    */
    shutterButtonAutoOuterColor?: string;
    shutterButtonIndicatorColor?: string;
    /**
    * The foreground color of the shutter button in manual mode.
    */
    shutterButtonManualInnerColor?: string;
    /**
    * The background color of the shutter button in manual mode.
    */
    shutterButtonManualOuterColor?: string;
    /**
    * Text hint that will be shown when the current detection status is net.doo.snap.lib.detector.DetectionResult.OK_BUT_BAD_ANGLES
    */
    textHintBadAngles?: string;
    /**
    * Text hint that will be shown when the current detection status is net.doo.snap.lib.detector.DetectionResult.OK_BUT_BAD_ASPECT_RATIO
    */
    textHintBadAspectRatio?: string;
    /**
    * Text hint that will be shown when the current detection status is net.doo.snap.lib.detector.DetectionResult.ERROR_NOTHING_DETECTED
    */
    textHintNothingDetected?: string;
    /**
    * Text hint that will be shown when the current detection status is net.doo.snap.lib.detector.DetectionResult.OK
    */
    textHintOK?: string;
    /**
    * Text hint that will be shown when the current detection status is net.doo.snap.lib.detector.DetectionResult.ERROR_TOO_DARK
    */
    textHintTooDark?: string;
    /**
    * Text hint that will be shown when the current detection status is net.doo.snap.lib.detector.DetectionResult.ERROR_TOO_NOISY
    */
    textHintTooNoisy?: string;
    /**
    * Text hint that will be shown when the current detection status is net.doo.snap.lib.detector.DetectionResult.OK_BUT_TOO_SMALL
    */
    textHintTooSmall?: string;
    /**
    * The background color of the top toolbar.
    */
    topBarBackgroundColor?: string;
    /**
    * The color of all active toggle buttons in the toolbar.
    */
    topBarButtonsActiveColor?: string;
    /**
    * The color of all inactive toggle buttons in the toolbar.
    */
    topBarButtonsInactiveColor?: string;
    /**
    * The background color of the user guidance hints.
    */
    userGuidanceBackgroundColor?: string;
    /**
    * The text color of the user guidance hints.
    */
    userGuidanceTextColor?: string;
}

export interface CroppingScreenConfiguration
{
    /**
    * Background color of the main screen.
    */
    backgroundColor?: string;
    /**
    * Background color of the bottom toolbar.
    */
    bottomBarBackgroundColor?: string;
    /**
    * Color of the titles of all buttons in the bottom toolbar (Rotate button).
    */
    bottomBarButtonsColor?: string;
    /**
    * Title of the cancel button.
    */
    cancelButtonTitle?: string;
    /**
    * Title of the Done button.
    */
    doneButtonTitle?: string;
    /**
    * Default color of the cropping outline.
    */
    polygonColor?: string;
    /**
    * Outline color of magnetically snapped edges.
    */
    polygonColorMagnetic?: string;
    /**
    * Width of the cropping outline.
    */
    polygonLineWidth?: number;
    /**
    * Title of the Rotate button.
    */
    rotateButtonTitle?: string;
    titleColor?: string;
    /**
    * Background color of the top toolbar.
    */
    topBarBackgroundColor?: string;
    /**
    * Color of the titles of all buttons in the top toolbar (Cancel and Done buttons).
    */
    topBarButtonsColor?: string;
    topBarTitle?: string;
}

export interface MrzScannerConfiguration
{
    bottomButtonsActiveColor?: string;
    bottomButtonsInactiveColor?: string;
    /**
    * Background color outside of the finder window.
    */
    cameraOverlayColor?: string;
    /**
    * Title of the cancel button.
    */
    cancelButtonTitle?: string;
    /**
    * Title of the button that opens the screen where the user can allow
    * the usage of the camera by the app.
    */
    enableCameraButtonTitle?: string;
    /**
    * Text that will be displayed when the app
    * is not allowed to use the camera, prompting the user
    * to enable the usage of the camera.
    */
    enableCameraExplanationText?: string;
    /**
    * Height of the finder window in pixels.
    */
    finderHeight?: number;
    /**
    * Color of the finder window's outline.
    */
    finderLineColor?: string;
    /**
    * Thickness of the finder window's outline.
    */
    finderLineWidth?: number;
    /**
    * Text hint shown under the finder window.
    */
    finderTextHint?: string;
    /**
    * Color of the text hint under the finder window.
    */
    finderTextHintColor?: string;
    /**
    * Width of the finder window in pixels.
    */
    finderWidth?: number;
    flashButtonTitle?: string;
    /**
    * Controls whether the flash should be initially enabled.
    * The default value is FALSE.
    */
    flashEnabled?: boolean;
    /**
    * Orientation lock mode of the camera: PORTRAIT or LANDSCAPE.
    * By default the camera orientation is not locked.
    */
    orientationLockMode?: CameraOrientationMode;
    /**
    * Controls whether to play a beep sound after a successful detection.
    * Default value is TRUE.
    */
    successBeepEnabled?: boolean;
    /**
    * Background color of the top toolbar.
    */
    topBarBackgroundColor?: string;
    /**
    * Color of the titles of all buttons in the top toolbar.
    */
    topBarButtonsColor?: string;
}

export interface BarcodeScannerConfiguration
{
    bottomButtonsActiveColor?: string;
    bottomButtonsInactiveColor?: string;
    /**
    * Background color outside of the finder window.
    */
    cameraOverlayColor?: string;
    /**
    * Title of the cancel button.
    */
    cancelButtonTitle?: string;
    /**
    * Title of the button that opens the screen where the user can allow
    * the usage of the camera by the app.
    */
    enableCameraButtonTitle?: string;
    /**
    * Text that will be displayed when the app
    * is not allowed to use the camera, prompting the user
    * to enable the usage of the camera.
    */
    enableCameraExplanationText?: string;
    /**
    * Height of the finder window in pixels.
    */
    finderHeight?: number;
    /**
    * Color of the finder window's outline.
    */
    finderLineColor?: string;
    /**
    * Thickness of the finder window's outline.
    */
    finderLineWidth?: number;
    /**
    * Text hint shown under the finder window.
    */
    finderTextHint?: string;
    /**
    * Color of the text hint under the finder window.
    */
    finderTextHintColor?: string;
    /**
    * Width of the finder window in pixels.
    */
    finderWidth?: number;
    flashButtonTitle?: string;
    /**
    * Controls whether the flash should be initially enabled.
    * The default value is FALSE.
    */
    flashEnabled?: boolean;
    /**
    * Orientation lock mode of the camera: PORTRAIT or LANDSCAPE.
    * By default the camera orientation is not locked.
    */
    orientationLockMode?: CameraOrientationMode;
    /**
    * Controls whether to play a beep sound after a successful detection.
    * Default value is TRUE.
    */
    successBeepEnabled?: boolean;
    /**
    * Background color of the top toolbar.
    */
    topBarBackgroundColor?: string;
    /**
    * Color of the titles of all buttons in the top toolbar.
    */
    topBarButtonsColor?: string;
}

export type BarcodeFormat =
    "ALL_FORMATS"
    | "AZTEC"
    | "CODABAR"
    | "CODE128"
    | "CODE39"
    | "CODE93"
    | "DATA_MATRIX"
    | "EAN13"
    | "EAN8"
    | "ITF"
    | "MAXICODE"
    | "PDF417"
    | "QR_CODE"
    | "RSS14"
    | "RSS_EXPANDED"
    | "UNKNOWN"
    | "UPC_A"
    | "UPC_E"
    | "UPC_EAN_EXTENSION"
;

export type CameraPreviewMode =
    "FILL_IN"
    | "FIT_IN"
;

export type CameraImageFormat =
    "JPG"
    | "PNG"
;

export type CameraOrientationMode =
    "NONE"
    | "PORTRAIT"
    | "PORTRAIT_UPSIDE_DOWN"
    | "LANDSCAPE_LEFT"
    | "LANDSCAPE_RIGHT"
    | "LANDSCAPE"
;

export type DetectionStatus = 
    "OK"
    | "OK_BUT_TOO_SMALL"
    | "OK_BUT_BAD_ANGLES"
    | "OK_BUT_BAD_ASPECT_RATIO"
    | "OK_BARCODE"
    | "ERROR_NOTHING_DETECTED"
    | "ERROR_TOO_DARK"
    | "ERROR_TOO_NOISY";

export type Status = "OK" | "CANCELED";

export type ImageFilter =
    "NONE"
    | "COLOR_ENHANCED"
    | "GRAYSCALE"
    | "BINARIZED"
    | "COLOR_DOCUMENT"
    | "PURE_BINARIZED"
    | "BACKGROUND_CLEAN"
    | "BLACK_AND_WHITE";

export type OCROutputFormat =
    "PLAIN_TEXT"
    | "PDF_FILE"
    | "FULL_OCR_RESULT";

export type MRZDocumentType =
    "PASSPORT"
    | "TRAVEL_DOCUMENT"
    | "VISA"
    | "ID_CARD"
    | "UNDEFINED";

export interface DocumentDetectionResult {
    detectionResult: DetectionStatus;
    polygon?: Point[];
    documentImageFileUri?: string;
}

export interface Point {
    x: number;
    y: number;
}

export interface Page {
    pageId: string;
    polygon: Point[];
    detectionResult: DetectionStatus;
    filter: ImageFilter;

    originalImageFileUri: string;
    documentImageFileUri?: string;
    originalPreviewImageFileUri: string;
    documentPreviewImageFileUri?: string;
}

export  interface DocumentScannerResult {
    status: Status;
    pages: Page[];
}

export  interface CroppingResult {
    status: Status;
    page?: Page;
}

export interface MrzRecognitionResult {
    recognitionSuccessful: boolean,
    documentType: MRZDocumentType,
    checkDigitsCount: number,
    validCheckDigitsCount: number,
    fields: {
        name: string;
        value: string;
        confidence: number;
    }[];
}

export interface MrzResult extends MrzRecognitionResult {
    status: Status;
}

export interface BarcodeResult {
    status: Status;
    format?: BarcodeFormat;
    value?: string;
}

export interface ScanbotSDKUI {
    startDocumentScanner(configuration: DocumentScannerConfiguration): Promise<DocumentScannerResult>;
    startCroppingScreen(page: Page, configuration: CroppingScreenConfiguration): Promise<CroppingResult>;
    startMrzScanner(configuration: MrzScannerConfiguration): Promise<MrzResult>;
    startBarcodeScanner(configuration: BarcodeScannerConfiguration): Promise<BarcodeResult>;
}

export interface ScanbotSDK {
    UI: ScanbotSDKUI;

    initializeSDK(options: {
        licenseKey: string;
        loggingEnabled?: boolean;
        storageImageQuality?: number;
        storageImageFormat?: CameraImageFormat;
    }): Promise<{result: string}>;
    isLicenseValid(): Promise<boolean>;
    detectDocument(imageFileUri: string): Promise<DocumentDetectionResult>;
    applyFilter(imageFileUri: string, filter: ImageFilter): Promise<string>;
    getOCRConfigs(): Promise<{
        languageDataPath: string;
        installedLanguages: string[];
    }>;
    performOCR(imageFileUris: string[], languages: string[], options: {
        outputFormat?: OCROutputFormat
    }): Promise<{
        plainText?: string;
        pdfFileUri?: string;
    }>;
    createPDF(imageFileUris: string[]): Promise<{
        pdfFileUri: string;
    }>;
    writeTIFF(imageFileUris: string[], tiffOptions: {
        oneBitEncoded?: boolean
    }): Promise<{
        tiffFileUri: string;
    }>;

    rotateImage(imageFileUri: string, degrees: number): Promise<string>;

    createPage(imageUri: string): Promise<Page>;
    setDocumentImage(page: Page, imageUri: string): Promise<Page>;

    detectDocumentOnPage(page: Page): Promise<Page>;
    applyImageFilterOnPage(page: Page, filter: ImageFilter): Promise<Page>;
    rotatePage(page: Page, times: number): Promise<Page>;
    getFilteredDocumentPreviewUri(page: Page, filter: ImageFilter): Promise<string>;

    recognizeMrz(imageFileUri: string): Promise<MrzRecognitionResult>;

    removePage(page: Page): Promise<void>;

    cleanup(): Promise<void>;
}

declare let ScanbotSDK: ScanbotSDK;

export default ScanbotSDK;
