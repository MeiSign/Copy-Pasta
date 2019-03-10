import React, { Component } from 'react';
import UploadForm from './UploadForm.js'

class Send extends Component {
  constructor(props) {
    super(props);
    this.state = {
      file: null,
      message: '',
    };
  }

  onFileChange(e) {
    this.setState({file:e.target.files[0]})
  }

  uploadFile(e) {
    e.preventDefault() // Stop form submit
    const data = new FormData();
    data.append('file', this.state.file);

    fetch("upload", {
      method: "POST",
      body: data
    }).then(res => {
      if (res.ok) {
        res.json().then(pointer => {
          this.setState({message: "Upload successful, scan the qr code to download."});
          this.props.onUpload(pointer);
        });
      } else {
        this.setState({message: "Upload failed."})
      }},
      err => 'Upload failed: ' + err
    )
  }

  render() {
    const message = this.state.message;

    return (
      <UploadForm onSubmit={this.uploadFile.bind(this)} onChange={this.onFileChange.bind(this)} message={message}/>
    );
  }
}

export default Send;
