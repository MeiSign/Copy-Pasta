import React, { Component } from 'react';
import UploadForm from './UploadForm.js'

class Send extends Component {

  render() {
    return (
      <UploadForm
        fileName={this.props.fileName}
        fileSize={this.props.fileSize}
        fileUnit={this.props.fileUnit}
        onSubmit={this.props.onUpload.bind(this)}
        onChange={this.props.onChange.bind(this)}/>
    );
  }
}

export default Send;
