import React, { Component } from 'react';
import { Row, Col } from 'react-flexbox-grid';

class UploadForm extends Component {
  render() {
    return (
      <Row>
        <Col xsOffset={1} xs={10} mdOffset={1} md={10} lgOffset={1} lg={10}>
          <p>{this.props.message}</p>
          <form encType="multipart/form-data" onSubmit={this.props.onSubmit}>
            <input type="file" name="file" onChange={this.props.onChange} />
            <input type="submit" value="Upload"/>
          </form>
        </Col>
      </Row>
    )
  }
}

export default UploadForm;
