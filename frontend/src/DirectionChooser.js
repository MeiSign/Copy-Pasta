import React, { Component } from 'react';
import { Row, Col } from 'react-flexbox-grid';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDesktop, faArrowRight, faArrowLeft, faMobileAlt } from '@fortawesome/free-solid-svg-icons'
import { faFile } from '@fortawesome/free-regular-svg-icons'

class DirectionButton extends Component {
  render() {
    const faIcons = this.props.icons;

    return (
      <Row className="Button" onClick={() => this.props.onClick()}>
        <Col xs={12} md={12} lg={12}>
          <Row className="Button-Label" center="xs" start="md">
            <Col xsOffset={0} mdOffset={1} lgOffset={1}>
              {faIcons.map((icon, i) => <FontAwesomeIcon key={i} icon={icon} />)}
            </Col>
            <Col>
              <span>{this.props.description}</span>
            </Col>
          </Row>
        </Col>
      </Row>
    );
  }
}

class DirectionChooser extends Component {
  chooseDirection = (sendTo) => {
    this.props.onDirectionChosen(sendTo);
  }

  render() {
    return (
      <Row>
        <Col xsOffset={1} xs={10} mdOffset={1} md={10} lgOffset={1} lg={10}>
          <DirectionButton icons={[faDesktop, faArrowRight, faFile, faArrowRight, faMobileAlt]} description={"Send to Mobile"} onClick={() => this.chooseDirection('send')} />
          <DirectionButton icons={[faDesktop, faArrowLeft, faFile, faArrowLeft, faMobileAlt]} description={"Receive from Mobile"} onClick={() => this.chooseDirection('receive')} />
        </Col>
      </Row>
    );
  }
}

export default DirectionChooser;
