/*
    Scanbot SDK ReactNative Module
    Copyright (c) 2017 doo GmbH. All rights reserved.
*/

import React, { Component } from 'react';
import { NativeModules, requireNativeComponent } from 'react-native';

const NativeScanbotCroppingView = requireNativeComponent('ScanbotCroppingView', null);
const NativeModule = NativeModules.ScanbotCroppingViewModule;

export default class ScanbotCroppingView extends React.Component {

  render() {
    return <NativeScanbotCroppingView {...this.props}/>
  }
  
  applyCroppingChanges() {
    NativeModule.applyCroppingChanges();
  }

  dismissCroppingChanges() {
    NativeModule.dismissCroppingChanges();
  }

  rotateImageClockwise() {
    NativeModule.rotateImageClockwise();
  }

}