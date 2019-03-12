import React, { Component } from 'react';
import { Row, Col } from 'react-flexbox-grid';

class UploadForm extends Component {
  constructor(props) {
    super(props);

    this.fileInput = React.createRef();
    this.submitButton = React.createRef();
  }

  render() {
    return (
      <form encType="multipart/form-data" onSubmit={this.props.onSubmit}>
      <input ref={this.fileInput} type="file" name="file" onChange={this.props.onChange} />
      <input ref={this.submitButton} type="submit" value="Upload"/>
        <Row>
          <Col xsOffset={1} xs={10} mdOffset={1} md={10} lgOffset={1} lg={10}>
            <Row middle="xs" center="xs">
              <Col className="BrowseButton" onClick={() => this.fileInput.current.click()}>
                Browse Files
              </Col>
              <Col xs={6} md={6} lg={6} className="FilePath">
                {this.props.fileName}
              </Col>
              <Col xs={2} md={2} lg={2} className="FilePath">
                {this.props.fileSize} {this.props.fileUnit}
              </Col>
            </Row>
            <Row center="xs">
              <Col className="Button" onClick={() => this.submitButton.current.click()}>
                Upload
              </Col>
            </Row>
          </Col>
        </Row>
      </form>
    )
  }
}

export default UploadForm;
