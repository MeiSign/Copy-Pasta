import React, { Component } from 'react';
import QRCode from 'qrcode.react'

class Download extends Component {
  render () {
    const url = this.props.downloadPath;

    return (
      <QRCode value={url} bgColor="#C0E5C8" fgColor="#454545"/>
    );
  }
}

export default Download;
