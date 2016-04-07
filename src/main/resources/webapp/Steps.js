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
var RUpdate = require('react-addons-update');

var sweetAlert = require('sweetalert');

var DatePicker = require('react-datepicker');
var moment = require('moment');

var Grid = require('react-bootstrap/lib/Grid');
var Row = require('react-bootstrap/lib/Row');
var Col = require('react-bootstrap/lib/Col');

var Button = require('react-bootstrap/lib/Button');
var FormControls = require('react-bootstrap/lib/FormControls');
var Input = require('react-bootstrap/lib/Input');
var ButtonInput = require('react-bootstrap/lib/ButtonInput');
var Modal = require('react-bootstrap/lib/Modal');

var restWithData = require('./RestUtils').restWithData
var $ = require('jquery')

var StepLinkEmpty = React.createClass({
  render: function() {
    return (
      <Row>
        <Col md={12}>
          <Button className="text-center" bsStyle="info" bsSize="large" block onClick={this.props.onNewLinkRequest}>Criar novo link</Button>
        </Col>
      </Row>
    )
  }
});

var locBool = function(b) {
  return b ? "Sim" : "Não"
}

var StepLink = React.createClass({
  getInitialState: function() {
    return { link: this.props.data }
  },

  handleUpdateLink: function(e) {
    e.preventDefault();
    var data = $.extend({}, this.state.link);
    delete data.usersId;
    delete data.timeCreated;
    console.log(data);
    this.props.onLinkUpdate(data);
  },

  handleTitleChange: function(e) {
    var newState = RUpdate(this.state, {
      link: {
        title: { $set: e.target.value }
      }
    });
    this.setState(newState);
  },
  handleUrlChange: function(e) {
    var newState = RUpdate(this.state, {
      link: {
        url: { $set: e.target.value }
      }
    });
    this.setState(newState);
  },
  handlePublishSelect: function(e) {
    var newState = RUpdate(this.state, {
      link: {
        published: { $set: e.target.value }
      }
    });
    this.setState(newState);
  },

  render: function() {
    var link = this.state.link;
    return (
      <div>
        <Row>
          <Col md={12}>
            <div className="well">
              <form onSubmit={this.handleUpdateLink}>
                <FormControls.Static label="id" value={link.id} />

                <Input type="text"
                  value={link.title}
                  placeholder="Título..."
                  label="Título"
                  onChange={this.handleTitleChange}/>

                <Input type="text"
                  value={link.url}
                  placeholder="http://..."
                  label="Url"
                  onChange={this.handleUrlChange}/>

                <Input type="select" label="Publicado" value={link.published} onChange={this.handlePublishSelect}>
                  <option value="true">Sim</option>
                  <option value="false">Não</option>
                </Input>
                <ButtonInput type="submit">Atualizar Link</ButtonInput>
              </form>
            </div>
          </Col>
        </Row>
      </div>
    )
  }
});

var StepLinkList = React.createClass({

  render: function() {
    var onLinkUpdate = this.props.onLinkUpdate;
    var list = this.props.data.map(function(link) {
      return (
        <StepLink key={link.id} data={link} onLinkUpdate={onLinkUpdate} />
      );
    });
    return (
      <div>
        <Row className="show-grid">
          <Col md={12}>
            <div className="page-header">
              <h1>Links associados a este passo</h1>
            </div>
          </Col>
        </Row>
        {list}
        <StepLinkEmpty onNewLinkRequest={this.props.onNewLinkRequest} />
      </div>
    );
  }
});

var Step = React.createClass({
  getInitialState: function() {
    return ({ showModal: false, showPanel: false, step: this.props.data });
  },
  handleOpenModal: function(e) {
    e.preventDefault();
    this.setState({ showModal: true });
  },
  handleStepModalClose: function(e) {
    this.setState({ showModal: false });
  },
  handleStepSubmit: function() {
    var step = $.extend(true, {}, this.state.step);
    delete step.usersId;
    delete step.links;
    console.log(step);
    this.props.onStepUpdate(step);
  },

  handleLinkUpdateMsg: function(link) {
    this.props.onLinkUpdate(link, this.state.step.id);
  },

  handleTitleChange: function(e) {
    var newState = RUpdate(this.state, {
      step: {
        title: { $set: e.target.value }
      }
    });
    this.setState(newState);
  },
  handleDescriptionChange: function(e) {
    var newState = RUpdate(this.state, {
      step: {
        description: { $set: e.target.value }
      }
    });
    this.setState(newState);
  },
  handlePositionChange: function(e) {
    var newState = RUpdate(this.state, {
      step: {
        position: { $set: e.target.value }
      }
    });
    this.setState(newState);
  },
  handlePublishSelect: function(e) {
    var newState = RUpdate(this.state, {
      step: {
        published: { $set: e.target.value }
      }
    });
    this.setState(newState);
  },
  handleCompletedSelect: function(e) {
    var newState = RUpdate(this.state, {
      step: {
        completed: { $set: e.target.value }
      }
    });
    this.setState(newState);
  },
  handlePossibleDateChange: function(e) {
    var newState = RUpdate(this.state, {
      step: {
        possibleDate: { $set: e.target.value }
      }
    });
    this.setState(newState);
  },
  handlePanelHeadClick: function(e) {
    this.setState({ showPanel: !this.state.showPanel });
  },
  render: function() {
    var step = this.state.step;
    var panelClassName = "panel " + (step.published ? "panel-success" : "panel-danger");
    var panelBodyClassName = "panel-body " + (this.state.showPanel ? "" : "collapse");
    return (
      <Row>
        <Col md={12}>
          <StepModal show={this.state.showModal} onStepModelClose={this.handleStepModalClose} onStepSubmit={this.handleStepSubmit} data={step} />
          <div className={panelClassName}>
            <div className="panel-heading" onClick={this.handlePanelHeadClick}>
              <h3 className="panel-title"><small>passo</small> {step.position} {step.title}</h3>
            </div>
            <div className={panelBodyClassName}>
              <form onSubmit={this.handleOpenModal}>
                <FormControls.Static label="id" value={step.id} />

                <Input type="textarea"
                  value={step.title}
                  placeholder="Título..."
                  label="Título"
                  onChange={this.handleTitleChange}
                  help="Será usado na tela principal (ideal seria algo curto)"/>

                <Input type="textarea"
                  value={step.description}
                  label="Descrição"
                  onChange={this.handleDescriptionChange}
                  placeholder="Descrição..." />

                <Input type="text"
                  value={step.position}
                  label="Posição do passo na tela principal"
                  help="Número. Passos seram ordenados usando esse número em ordem crescente."
                  onChange={this.handlePositionChange}
                  placeholder="Exemplo: 2" />

                <Input type="text"
                  value={step.possibleDate}
                  label="Possível data de conclusão"
                  onChange={this.handlePossibleDateChange}
                  placeholder="Exemplo: 1° de abril" />

                <Input type="select" label="Passo concluído?" value={step.completed} onChange={this.handleCompletedSelect}>
                  <option value="true">Sim</option>
                  <option value="false">Não</option>
                </Input>

                <Input type="select" label="Publicado?" value={step.published} onChange={this.handlePublishSelect}>
                  <option value="true">Sim</option>
                  <option value="false">Não</option>
                </Input>

                <ButtonInput type="submit"
                  value="Atualizar o passo"
                  bsStyle="primary"
                  bsSize="large" />

              </form>


              <StepLinkList data={step.links} onNewLinkRequest={this.props.onNewLinkRequest.bind(null, step.id) } onLinkUpdate={this.handleLinkUpdateMsg} />

            </div>
          </div>
        </Col>
      </Row>
    );
  }
});

var StepModal = React.createClass({
  handleSubmit: function(e) {
    e.preventDefault();
    this.props.onStepModelClose();
    this.props.onStepSubmit();
  },
  render: function() {
    var step = this.props.data;
    return (
      <Modal show={this.props.show} onHide={this.props.onStepModelClose}>
        <Modal.Header closeButton>
          <Modal.Title>Confirme apenas se os dados estiverem corretos...</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Grid>
            <Row>
              <Col md={10}>

                <form onSubmit={this.handleSubmit}>
                  <FormControls.Static label="id" value={step.id} />

                  <FormControls.Static label="Título" value={step.title} />
                  <FormControls.Static label="Descrição" value={step.description} />
                  <FormControls.Static label="Posição" value={step.position} />
                  <FormControls.Static label="Possível data de conclusão"  value={step.possibleDate} />
                  <FormControls.Static label="Completado?"  value={locBool(step.completed) } />
                  <FormControls.Static label="Publicado" value={locBool(step.published) } />

                  <ButtonInput type="submit"
                    value="Confirmar"
                    bsStyle="primary"
                    bsSize="large" />
                </form>
              </Col>
            </Row>
          </Grid>

        </Modal.Body>
        <Modal.Footer>
          <ButtonInput onClick={this.props.onStepModelClose}>Fechar</ButtonInput>
        </Modal.Footer>
      </Modal>
    );
  }
});


var StepList = React.createClass({
  render: function() {
    var onLinkUpdate = this.props.onLinkUpdate;
    var onNewLinkRequest = this.props.onNewLinkRequest;
    var stepUpdateFunction = this.props.onStepUpdate;
    var showNotPublishedItems = this.props.showNotPublishedItems;
    var list = this.props.data.map(function(step) {
      if (showNotPublishedItems || step.published) {
        return (
          <Step key={step.id} data={step} onStepUpdate={stepUpdateFunction} onNewLinkRequest={onNewLinkRequest} onLinkUpdate={onLinkUpdate} />
        );
      } else {
        return <div key={step.id}></div>
      }
    });
    return (
      <div>
        <Row className="show-grid">
          <Col md={12}>
            <div className="page-header">
              <h1>Lista dos passos do impeachment</h1>
            </div>
          </Col>
        </Row>
        {list}
      </div>
    );
  }
});

var StepRepository = React.createClass({
  getInitialState: function() {
    return ({ data: [] });
  },

  createNewStepData: function() {
    return ({
      title: 'Título temporário de passo recem criado',
      description: 'Descrição temporária de passo recem criado.',
      position: 50,
      possibleDate: '1° de abril'
    })
  },

  createNewLinkData: function() {
    return ({
      title: 'Google',
      url: 'http://google.com',
    })
  },

  loadStepsFromServer: function() {
    restWithData(this.props.serverPath + "steps/editor", 'GET',
      this.props.credential, null,
      function(data) {
        //sweetAlert("Sucesso", "Informações do servidor coletadas de forma correta.");
        this.setState({ data: data });
      }.bind(this),
      function(xhr, status, err) {
        console.error(status);
        console.error(err);
        sweetAlert("Erro", status + ", " + xhr.responseText);
      }.bind(this)
    );
  },

  updateStep: function(step) {
    console.log(step);
    restWithData(this.props.serverPath + "steps/" + step.id, 'PUT',
      this.props.credential, step,
      function(data) {
        sweetAlert("Sucesso", JSON.stringify(data, null, 2));
        this.loadStepsFromServer();
      }.bind(this),
      function(xhr, status, err) {
        console.error(status);
        console.error(err);
        sweetAlert("Erro", status + ", " + xhr.responseText);
      }.bind(this)
    );
  },

  handleNewStepRequest: function() {
    var newStepData = this.createNewStepData();
    restWithData(this.props.serverPath + "steps", 'POST',
      this.props.credential, newStepData,
      function(data) {
        sweetAlert("Sucesso", JSON.stringify(data));
        this.loadStepsFromServer();
      }.bind(this),
      function(xhr, status, err) {
        console.error(status);
        console.error(err);
        sweetAlert("Erro", status + ", " + xhr.responseText);
      }.bind(this)
    );
  },

  handleNewLinkRequest: function(stepId) {
    var newLinkData = this.createNewLinkData();

    restWithData(this.props.serverPath + "steps/" + stepId + "/links",
      'POST', this.props.credential,
      newLinkData,
      function(data) {
        sweetAlert("Sucesso", JSON.stringify(data));
        this.loadStepsFromServer();
      }.bind(this),
      function(xhr, status, err) {
        console.error(status);
        console.error(err);
        sweetAlert("Erro", status + ", " + xhr.responseText);
      }.bind(this)
    );
  },

  handleLinkUpdate: function(link, stepId) {
    restWithData(this.props.serverPath + "steps/" + stepId + "/links/" + link.id,
      'PUT', this.props.credential,
      link,
      function(data) {
        sweetAlert("Sucesso", JSON.stringify(data));

        this.loadStepsFromServer();
      }.bind(this),
      function(xhr, status, err) {
        console.error(status);
        console.error(err);
        sweetAlert("Erro", status + ", " + xhr.responseText);
      }.bind(this));
  },

  render: function() {
    return (
      <div>
        <StepList key={moment().valueOf() } data={this.state.data}
          onStepUpdate={this.updateStep} onNewLinkRequest={this.handleNewLinkRequest} onLinkUpdate={this.handleLinkUpdate}
          showNotPublishedItems={this.props.showNotPublishedItems} />
        <StepEmpty onNewStepRequest={this.handleNewStepRequest} />
      </div>
    );
  }
});


var StepEmpty = React.createClass({
  render: function() {
    return (
      <Row>
        <Col md={12}>
          <div className="panel panel-info">
            <div className="panel-heading">
              <h3 className="panel-title">Criar um novo passo</h3>
            </div>
            <div className="panel-body">
              <Button className="text-center" bsStyle="primary" bsSize="large" block onClick={this.props.onNewStepRequest}>Criar novo passo</Button>
            </div>
          </div>
        </Col>
      </Row>
    )
  }
});


module.exports = {
  StepRepository: StepRepository
}