import React, { Component } from 'react';
import DirectionChooser from './DirectionChooser.js';
import Send from './Send.js';
import Receive from './Receive.js';
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
      message: '',
    };
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
        this.setState({message: "Upload not found."})
      }
    },
    err => 'Upload failed: ' + err
    )
  }

  handleDirectionChosen = (direction) => {
    this.setState({
      direction: direction
    });
  }

  handleChange = (e) => {
    const target = e.target;
    const value = target.type === 'file' ? target.files[0] : target.value;
    const name = target.name;

    this.setState({
      [name]: value
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
          this.setState({message: "Upload successful, scan the qr code to download."});
          this.handleDesktopUpload(pointer);
        });
      } else {
        this.setState({message: "Upload failed."})
      }},
      err => 'Upload failed: ' + err
    )
  }

  render() {
    const direction = this.state.direction;
    const downloadPath = this.state.downloadPath;
    const uploadUuid = this.state.uploadUuid;
    let download, send, receive, directionChooser;

    if (direction === null && !uploadUuid) {
      directionChooser = <DirectionChooser
        onDirectionChosen={(direction) => this.handleDirectionChosen(direction)}/>
    } else if (direction === 'send') {
      if (downloadPath) {
        download = <ResponsiveQrCode url={downloadPath} />
      } else {
        send = <Send
          onUpload={(form) => this.handleUploadFile(form)}
          onChange={(file) => this.handleChange(file)}/>
      }
    } else if (direction === 'receive' || uploadUuid) {
      receive = <Receive
        onAwaitDownload={(pointer) => this.handleAwaitDownload(pointer)}
        onUpload={(e) => this.handleUploadFile(e)}
        onChange={(e) => this.handleChange(e)}
        uploadUuid={uploadUuid} />
    }

    return (
      <Grid fluid>
        <Row>
          <Col xsOffset={1} xs={10} mdOffset={1} md={10} lgOffset={3} lg={6} className="Header Content">
            <Row middle="xs">
              <Col xsOffset={1} xs={7} mdOffset={1} md={7} lgOffset={1} lg={7}>
                <h1><FontAwesomeIcon icon={faCopy} /> Copy Pasta</h1>
              </Col>
              <Col xs={4} md={4} lg={4}>
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
          <Col xsOffset={1} xs={10} mdOffset={1} md={10} lgOffset={3} lg={6} className="Main Content">
            {directionChooser}
            {send}
            {receive}
            {download}
          </Col>
        </Row>
        <Row>
          <Col xsOffset={1} xs={10} mdOffset={1} md={10} lgOffset={3} lg={6} className="Footer Content">
            <Row>
              <Col xsOffset={1} xs={10} mdOffset={1} md={10} lgOffset={1} lg={10}>
                <button onClick={() => this.setState({direction: null})}>Back</button>
              </Col>
            </Row>
          </Col>
        </Row>
      </Grid>

    );
  }
}

export default App;
