/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/

/**
 * @providesModule react-native-scanbot-sdk
 */

import { NativeModules } from 'react-native';
import ScanbotCameraView from './ScanbotCameraView.js';
import ScanbotCroppingView from './ScanbotCroppingView.js';

let ScanbotSDK = NativeModules.ScanbotSDK;

const ImageFilter = {
  COLOR_ENHANCED: "COLOR_ENHANCED",
  COLOR_DOCUMENT: "COLOR_DOCUMENT",
  BINARIZED: "BINARIZED",
  GRAYSCALE: "GRAYSCALE"
};

const OCROutputFormat = {
  PLAIN_TEXT: "PLAIN_TEXT",
  PDF_FILE: "PDF_FILE",
  FULL_OCR_RESULT: "FULL_OCR_RESULT"
};

module.exports = {
  ScanbotSDK,
  ImageFilter,
  OCROutputFormat,
  ScanbotCameraView,
  ScanbotCroppingView
};
