import React, { Component } from 'react';
import MobileUpload from './MobileUpload.js';
import DesktopUpload from './DesktopUpload.js';
import ChoosePlatform from './ChoosePlatform.js';
import Download from './Download.js';

class App extends Component {
  constructor(props) {
    super(props);
    let params = new URLSearchParams(window.location.search);
    this.state = {
      downloadPath: null,
      mobile: params.get("uuid"),
      desktop: false,
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

  handlePlatformChosen = (platform) => {
    console.log("Upload from " + platform + " chosen.")
    this.setState({
      mobile: platform === "mobile",
      desktop: platform === "desktop"
    });
  }

  render() {
    const downloadPath = this.state.downloadPath;
    const desktop = this.state.desktop;
    const mobile = this.state.mobile;
    const uploadUuid = this.state.uploadUuid;
    let download, desktopUpload, mobileUpload, choosePlatform;

    if (!desktop && !mobile) { choosePlatform = <ChoosePlatform onPlatformChosen={(platform) => this.handlePlatformChosen(platform)}/> }
    if (desktop) { desktopUpload = <DesktopUpload onUpload={(pointer) => this.handleDesktopUpload(pointer)} />}
    if (mobile) { mobileUpload = <MobileUpload onAwaitDownload={(pointer) => this.handleAwaitDownload(pointer)} uploadUuid={uploadUuid} />}
    if (downloadPath) { download = <Download downloadPath={downloadPath} /> }
    return (
      <div className="App">
          {choosePlatform}
          {desktopUpload}
          {mobileUpload}
          {download}
      </div>
    );
  }
}

export default App;
