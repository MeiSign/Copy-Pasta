import React, { Component } from 'react';
import * as Uuidv4 from 'uuid/v4';
import QRCode from 'qrcode.react';
import { Row, Col } from 'react-flexbox-grid';
import Measure from 'react-measure';

class Receive extends Component {
  constructor(props) {
    super(props);

    this.state = {
      file: null,
      message: (this.props.uploadUuid) ? 'Upload your file and your desktop will be redirected to your download.' : 'Scan the qr code to upload your file' ,
      newUuid: (!this.props.uploadUuid) ? Uuidv4() : undefined,
      dimensions: {
        width: -1,
        height: -1,
      }
    };

    this.awaitDownload();
  }

  onFileChange(e) {
    this.setState({file: e.target.files[0]})
  }

  uploadFile(e) {
    e.preventDefault() // Stop form submit
    const data = new FormData();
    data.append('file', this.state.file);

    fetch("upload?uuid=" + this.props.uploadUuid, {
      method: "POST",
      body: data
    }).then(res => {
      if (res.ok) {
        res.json().then(pointer => {
          this.setState({message: "Upload successful, your desktop will be redirected to the download."});
        });
      } else {
        this.setState({message: "Upload failed."})
      }
    },
    err => 'Upload failed: ' + err
    );
  }

  awaitDownload(uuid) {
    if(!this.props.uploadUuid) {
      fetch("awaitDownload/" + this.state.newUuid, {
        method: "GET"
      }).then(res => {
        if (res.ok) {
          res.json().then(pointer => {
            this.props.onAwaitDownload(pointer);
          });
        } else {
          this.setState({message: "Upload not found."})
        }
      },
      err => 'Upload failed: ' + err
      )
    }
  }

  render() {
    const message = this.state.message;
    const uuid = this.props.uploadUuid;
    const newUuid = this.state.newUuid;
    const width = this.state.dimensions.width

    let uploadWidget;
    if (uuid) {
      uploadWidget =
        <form encType="multipart/form-data" onSubmit={this.uploadFile.bind(this)}>
          <input type="file" name="file" onChange={this.onFileChange.bind(this)} />
          <input type="submit" value="Upload"/>
        </form>
    } else {
      uploadWidget = <Row>
        <Measure
          bounds
          onResize={contentRect => { this.setState({ dimensions: contentRect.bounds }) }}>

          {({ measureRef }) => (
            <Col xsOffset={2} xs={8} mdOffset={3} md={6} lgOffset={3} lg={6}>
              <div ref={measureRef}>
              <QRCode
                value={window.location.origin + "?uuid=" + newUuid}
                bgColor="#ECECEC"
                fgColor="#000000"
                size={width}/>
              </div>
            </Col>
          )}
        </Measure>
      </Row>
    }

    return (
      <div>
        <p>{message}</p>
        {uploadWidget}
      </div>
    );
  }
}

export default Receive;
