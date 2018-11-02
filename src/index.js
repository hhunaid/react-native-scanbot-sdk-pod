/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/

/**
 * @providesModule react-native-scanbot-sdk
 */

import { NativeModules } from 'react-native';

let ScanbotSDK = NativeModules.ScanbotSDK;
ScanbotSDK.UI = NativeModules.SBSDKDefaultUi;

module.exports = ScanbotSDK;