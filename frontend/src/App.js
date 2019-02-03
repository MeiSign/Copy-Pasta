import React, { Component } from 'react';
import Upload from './Upload.js';
import Download from './Download.js';

class App extends Component {
  constructor(props) {
    super(props);
    this.state = {
      downloadPath: null,
    };
  }


  handleUpload(pointer) {
    const path = window.location.origin + "/download/" + pointer.uuid + "/" + pointer.key;
    this.setState({downloadPath: path});
  }

  render() {
    const downloadPath = this.state.downloadPath;
    let download;
    if (downloadPath) { download = <Download downloadPath={downloadPath} /> }
    return (
      <div className="App">
        <Upload
          onUpload={(pointer) => this.handleUpload(pointer)}
        />
        {download}
      </div>
    );
  }
}

export default App;
