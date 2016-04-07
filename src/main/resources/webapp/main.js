/*******************************************************************************
 * Copyright 2016 Allan Yoshio Hasegawa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
var React = require('react');
var ReactDOM = require('react-dom');

var Grid = require('react-bootstrap/lib/Grid');
var Row = require('react-bootstrap/lib/Row');
var Col = require('react-bootstrap/lib/Col');

var Button = require('react-bootstrap/lib/Button');

var UserCredentials = require('./UserCredentials').UserCredentials

var ImportantNewsRepository = require('./ImportantNews').ImportantNewsRepository

var StepRepository = require('./Steps.js').StepRepository

var ServerUrl = require('./ServerInfo').ServerUrl

var MainSite = React.createClass({
  getInitialState: function() {
    return ({
      credential: {
        username: "",
        password: ""
      },
      serverPath: ServerUrl(),
      showNotPublishedItems: true
    })
  },

  handleCredentialChange: function(username, password) {
    this.setState({credential: {
      username: username,
      password: password
    }});
  },
  
  handleUpdatePage: function() {
    this._sr.loadStepsFromServer();
    this._nr.loadNewsFromServer();
  },
  
  handleShowNonPublished: function() {
    this.setState({showNotPublishedItems: true})
  },
  
  handleHideNonPublished: function() {
    this.setState({showNotPublishedItems: false})
  },

  hideShowPublishedButton: function() {
    if (this.state.showNotPublishedItems) {
      return (
        <Button className="btn-info" block onClick={this.handleHideNonPublished}>Esconder items não publicados</Button>
      )
    } else {
      return (
        <Button className="btn-danger" block onClick={this.handleShowNonPublished}>Mostrar items não publicados</Button>
      )
    }
  },
  
  render: function() {
    return (
      <Grid>
        <UserCredentials credential={this.state.credential} onCredentialChange={this.handleCredentialChange} />
        <Row>
          <Col md={5} mdOffset={3}>
            <Button className="btn-primary" block onClick={this.handleUpdatePage}>Pegar informações do servidor</Button>
          </Col>
        </Row>
        <Row style={{marginTop: 60 + 'px'}}>
        </Row>
        <Row>
          <Col md={7} mdOffset={2}>
            {this.hideShowPublishedButton()}
          </Col>
        </Row>
        <Row>
          <Col md={6}>
            <StepRepository ref={function(sr) { this._sr = sr; }.bind(this) } serverPath={this.state.serverPath} credential={this.state.credential} showNotPublishedItems={this.state.showNotPublishedItems}/>
          </Col>
          <Col md={6}>
            <ImportantNewsRepository ref={function(nr) { this._nr = nr; }.bind(this) } serverPath={this.state.serverPath} credential={this.state.credential} showNotPublishedItems={this.state.showNotPublishedItems}/>
          </Col>
        </Row>
      </Grid>
    );
  }
});

ReactDOM.render(
  <MainSite />,
  document.getElementById('content')
);