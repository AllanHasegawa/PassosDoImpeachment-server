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
var FormControls = require('react-bootstrap/lib/FormControls');
var Input = require('react-bootstrap/lib/Input');
var ButtonInput = require('react-bootstrap/lib/ButtonInput');
var Modal = require('react-bootstrap/lib/Modal');
var $ = require('jquery')


var UserCredentials = React.createClass({
  handleUsernameChange: function(e) {
    this.props.onCredentialChange(e.target.value, this.props.credential.password);
  },
  handlePasswordChange: function(e) {
    this.props.onCredentialChange(this.props.credential.username, e.target.value);
  },
  render: function() {
    return (
      <div>
        <Row className="show-grid">
          <Col xs={12}>
            <div className="page-header">
              <h1>Credenciais <small>insira usuário/senha para fazer edição do conteúdo</small></h1>
            </div>
          </Col>
        </Row>
        <Row className="show-grid">
          <Col xs={5} xsOffset={3}>
            <form className="form-horizontal">
              <Input type="text" label="Usuário" labelClassName="col-xs-2" wrapperClassName="col-xs-10" value={this.props.credential.username} onChange={this.handleUsernameChange} />
              <Input type="password" label="Senha" labelClassName="col-xs-2" wrapperClassName="col-xs-10" value={this.props.credential.password} onChange={this.handlePasswordChange} />
            </form>
          </Col>
        </Row>
      </div>
    );
  }
});

module.exports = {
  UserCredentials: UserCredentials
}