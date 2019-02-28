import React, { Component } from 'react';

class ChoosePlatform extends Component {
  chooseDesktop() {
    this.props.onPlatformChosen("desktop");
  }


  chooseMobile() {
    this.props.onPlatformChosen("mobile");
  }

  render() {
    return (
      <div>
        <button type="button" onClick={this.chooseMobile.bind(this)}>Upload from Mobile</button>
        <button type="button" onClick={this.chooseDesktop.bind(this)}>Upload from Desktop</button>
      </div>
    );
  }
}

export default ChoosePlatform;
