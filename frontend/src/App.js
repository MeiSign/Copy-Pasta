import React, { Component } from 'react';
import MobileUpload from './MobileUpload.js';
import DesktopUpload from './DesktopUpload.js';
import DirectionChooser from './DirectionChooser.js';
import Download from './Download.js';
import { Grid, Row, Col } from 'react-flexbox-grid';

class App extends Component {
  constructor(props) {
    super(props);
    let params = new URLSearchParams(window.location.search);
    this.state = {
      downloadPath: null,
      direction: null,
      uploadUuid: params.get("uuid")
    };
  }

  handleDesktopUpload(pointer) {
    const path = window.location.origin + "/download/" + pointer.uuid + "/" + pointer.key;
    this.setState({downloadPath: path});
  }

  handleAwaitDownload(pointer) {
    const path = window.location.origin + "/download/" + pointer.uuid + "/" + pointer.key;
    let a = document.createElement('a');
    a.href = path;
    a.click();
  }

  handleDirectionChosen = (direction) => {
    console.log(direction + " to/from mobile")
    this.setState({
      direction: direction
    });
  }

  render() {
    const direction = this.state.direction;
    const downloadPath = this.state.downloadPath;
    const uploadUuid = this.state.uploadUuid;
    let download, desktopUpload, mobileUpload, directionChooser;

    if (direction === null) { directionChooser = <DirectionChooser onDirectionChosen={(direction) => this.handleDirectionChosen(direction)}/> }
    if (direction === 'send') { desktopUpload = <DesktopUpload onUpload={(pointer) => this.handleDesktopUpload(pointer)} />}
    if (direction === 'receive') { mobileUpload = <MobileUpload onAwaitDownload={(pointer) => this.handleAwaitDownload(pointer)} uploadUuid={uploadUuid} />}
    if (downloadPath) { download = <Download downloadPath={downloadPath} /> }
    return (
      <Grid fluid>
        <Row>
          <Col xsOffset={1} xs={10} mdOffset={1} md={10} lgOffset={3} lg={6} className="Header Content">
            <Row>
              <Col xsOffset={1} xs={10} mdOffset={1} md={10} lgOffset={1} lg={10}>
                <h1>Copy Pasta</h1>
              </Col>
            </Row>
          </Col>
        </Row>
        <Row className="App">
          <Col xsOffset={1} xs={10} mdOffset={1} md={10} lgOffset={3} lg={6} className="Main Content">
            {directionChooser}
            {desktopUpload}
            {mobileUpload}
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
