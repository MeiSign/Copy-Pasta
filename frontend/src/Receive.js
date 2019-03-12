import React, { Component } from 'react';
import * as Uuidv4 from 'uuid/v4';
import ResponsiveQrCode from './ResponsiveQrCode.js'
import UploadForm from './UploadForm.js'

class Receive extends Component {
  constructor(props) {
    super(props);

    this.state = {
      newUuid: (!this.props.uploadUuid) ? Uuidv4() : undefined
    };

    if (this.state.newUuid) {
      this.props.onAwaitDownload(this.state.newUuid);
    }
  }

  render() {
    const uuid = this.props.uploadUuid;
    const newUuid = this.state.newUuid;

    if (uuid) {
      return <UploadForm
        uuid={uuid}
        fileName={this.props.fileName}
        fileSize={this.props.fileSize}
        fileUnit={this.props.fileUnit}
        onSubmit={this.props.onUpload.bind(this)}
        onChange={this.props.onChange.bind(this)}/>;
    } else {
      return <ResponsiveQrCode url={window.location.origin + "?uuid=" + newUuid}/>;
    }
  }
}

export default Receive;
