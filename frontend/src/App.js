import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';

class App extends Component {
  state = {};

  componentDidMount() {
      setInterval(this.hello, 250);
  }

  hello = () => {
      fetch('/api/test')
          .then(response => response.text())
          .then(message => {
              this.setState({message: message});
          });
  };

  render() {
    return (
      <div className="App">
        <header className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
          <p>
            <h1>Hi there</h1>
            <p className="App-title">{this.state.message}</p>
          </p>
        </header>
      </div>
    );
  }
}

export default App;
