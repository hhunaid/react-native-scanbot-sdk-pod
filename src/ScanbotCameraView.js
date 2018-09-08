/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/

import React, { Component } from 'react';
import { NativeModules, requireNativeComponent } from 'react-native';

const NativeScanbotCameraView = requireNativeComponent('ScanbotCameraView', null);
const NativeManager = NativeModules.ScanbotCameraViewManager;

export default class ScanbotCameraView extends React.Component {

  render() {
    return <NativeScanbotCameraView {...this.props}/>
  }
  
}