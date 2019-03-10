import React, { Component } from 'react';
import QRCode from 'qrcode.react';
import { Row, Col } from 'react-flexbox-grid';
import Measure from 'react-measure';

class ResponsiveQrCode extends Component {
  constructor(props) {
    super(props);

    this.state = {
      dimensions: {
        width: -1,
        height: -1,
      }
    };
  }

  render () {
    const width = this.state.dimensions.width;
    const url = this.props.url;

    return (
      <Row>
        <Measure
          bounds
          onResize={contentRect => { this.setState({ dimensions: contentRect.bounds }) }}>

          {({ measureRef }) => (
            <Col xsOffset={2} xs={8} mdOffset={3} md={6} lgOffset={3} lg={6}>
              <div ref={measureRef}>
              <QRCode value={url} bgColor="#ECECEC" fgColor="#000000" size={width}/>
              </div>
            </Col>
          )}
        </Measure>
      </Row>
    );
  }
}

export default ResponsiveQrCode;
