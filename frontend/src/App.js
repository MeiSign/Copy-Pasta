import React, { Component } from 'react';
import DirectionChooser from './DirectionChooser.js';
import Send from './Send.js';
import Receive from './Receive.js';
import Message from './Message.js';
import ResponsiveQrCode from './ResponsiveQrCode.js';
import { Grid, Row, Col } from 'react-flexbox-grid';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCopy } from '@fortawesome/free-regular-svg-icons'


class App extends Component {
  constructor(props) {
    super(props);
    let params = new URLSearchParams(window.location.search);
    this.state = {
      downloadPath: null,
      direction: null,
      uploadUuid: params.get("uuid"),
      file: null,
      fileName: 'No file selected',
      fileSize: 0,
      fileUnit: 'b',
      message: '',
    };
  }

  resetState = () => {
    let params = new URLSearchParams(window.location.search);
    this.setState({
      downloadPath: null,
      direction: null,
      uploadUuid: params.get("uuid"),
      file: null,
      fileName: 'No file selected',
      fileSize: 0,
      fileUnit: 'b',
      messageText: '',
    });
  }

  handleDesktopUpload = (pointer) => {
    const path = window.location.origin + "/download/" + pointer.uuid + "/" + pointer.key;
    this.setState({downloadPath: path});
  }

  downloadReceived = (pointer) => {
    const path = window.location.origin + "/download/" + pointer.uuid + "/" + pointer.key;
    let a = document.createElement('a');
    a.href = path;
    a.click();
  }

  handleAwaitDownload = (uuid) => {
    fetch("awaitDownload/" + uuid, {
      method: "GET"
    }).then(res => {
      if (res.ok) {
        res.json().then(pointer => {
          this.downloadReceived(pointer);
        });
      } else {
        this.setState({messageText: "Upload not found."})
      }
    },
    err => 'Upload failed: ' + err
    )
  }

  handleDirectionChosen = (direction) => {
    const messageText = direction === 'send' ?
      'Please select the file you would like to upload.' :
      'Scan the QR code with the device from which you would like to receive a file.';

    this.setState({
      messageText: messageText,
      direction: direction
    });
  }

  setFileSize = (bytes) => {
    if (bytes === 0) return
    if ((bytes / 1024 / 1024) > 1) {
      this.setState({
        fileSize: (bytes / 1024 / 1024).toFixed(2),
        fileUnit: 'mb'
      })
    } else if ((bytes / 1024) > 1) {
      this.setState({
        fileSize: (bytes / 1024).toFixed(2),
        fileUnit: 'kb'
      })
    } else {
      this.setState({
        fileSize: bytes,
        fileUnit: 'b'
      })
    }
  }

  handleChange = (e) => {
    const target = e.target;
    const value = target.files[0];
    const name = target.name;

    this.setFileSize(value.size)
    this.setState({
      [name]: value,
      fileName: value.name
    });
  }

  handleUploadFile = (e) => {
    e.preventDefault();

    const uploadUuid = this.state.uploadUuid;
    const data = new FormData();
    data.append('file', this.state.file);

    let action;
    if (uploadUuid) {
      action = "upload?uuid=" + uploadUuid;
    } else {
      action = "upload";
    }

    fetch(action, {
      method: "POST",
      body: data
    }).then(res => {
      if (res.ok) {
        res.json().then(pointer => {
          this.setState({
            messageText:
              "Upload successful, you can now use your other device to scan the qr code and download the file."
          });
          this.handleDesktopUpload(pointer);
        });
      } else {
        this.setState({messageText: "Upload failed. Please try again later."});
      }},
      err =>
        this.setState({messageText: "Upload failed. Please try again later."})
    )
  }

  render() {
    const direction = this.state.direction;
    const downloadPath = this.state.downloadPath;
    const uploadUuid = this.state.uploadUuid;
    const messageText = this.state.messageText;
    let download, send, receive, directionChooser, message;

    if (messageText) {
      message = <Message message={messageText} />;
    }

    if (direction === null && !uploadUuid) {
      directionChooser = <DirectionChooser
        onDirectionChosen={(direction) => this.handleDirectionChosen(direction)}/>
    } else if (direction === 'send') {
      if (downloadPath) {
        download = <ResponsiveQrCode url={downloadPath} />
      } else {
        send = <Send
          fileName={this.state.fileName}
          fileSize={this.state.fileSize}
          fileUnit={this.state.fileUnit}
          onUpload={(form) => this.handleUploadFile(form)}
          onChange={(file) => this.handleChange(file)}/>
      }
    } else if (direction === 'receive' || uploadUuid) {
      receive = <Receive
        fileName={this.state.fileName}
        fileSize={this.state.fileSize}
        fileUnit={this.state.fileUnit}
        onAwaitDownload={(pointer) => this.handleAwaitDownload(pointer)}
        onUpload={(e) => this.handleUploadFile(e)}
        onChange={(e) => this.handleChange(e)}
        uploadUuid={uploadUuid} />
    }

    return (
      <Grid style={{height:100 + 'vh'}}>
        <Row>
          <Col xs={12} md={12} lg={12} className="Header Content">
            <Row middle="xs">
              <Col xsOffset={1} xs={8} mdOffset={1} md={8} lgOffset={1} lg={8}>
                <h1><a href="/"><FontAwesomeIcon icon={faCopy} /> Copy Pasta</a></h1>
              </Col>
              <Col xs={3} md={3} lg={3}>
                <a href="https://github.com/MeiSign/Copy-Pasta" alt="Available on GitHub">
                  <img src="https://img.shields.io/github/license/MeiSign/Copy-Pasta.svg" alt="MIT License"/>
                </a>
                <a href="https://github.com/MeiSign/Copy-Pasta" alt="Available on GitHub">
                  <img src="https://img.shields.io/github/stars/MeiSign/Copy-Pasta.svg?label=GitHub&style=social" alt="Follow on Github"/>
                </a>
              </Col>
            </Row>
          </Col>
        </Row>
        <Row className="App">
          <Col xs={12} md={12} lg={12} className="Main Content">
            {message}
            {directionChooser}
            {send}
            {receive}
            {download}
          </Col>
        </Row>
        <Row>
          <Col xs={12} md={12} lg={12} className="Footer Content">
            <Row>
              <Col xsOffset={1} xs={10} mdOffset={1} md={10} lgOffset={1} lg={10}>
                <button onClick={this.resetState}>Back</button>
              </Col>
            </Row>
          </Col>
        </Row>
      </Grid>

    );
  }
}

export default App;
