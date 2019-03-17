import React, { Component } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCog } from '@fortawesome/free-solid-svg-icons'

class LoadingCircle extends Component {

  render() {
    return (
      <div class="LoadingBox">
        <p><FontAwesomeIcon icon={faCog} spin size="4x"/></p>
        Uploading
      </div>
    );
  }
}

export default LoadingCircle;
