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
var $ = require('jquery')


var restCall = require('./RestUtils').restWithData

var locBool = function(b) {
  return b ? "Sim" : "Não"
}
var ImportantNewsModal = React.createClass({
  handleSubmit: function(e) {
    e.preventDefault();
    this.props.onModalClose();
    this.props.onNewsSubmit();
  },
  render: function() {
    var news = this.props.data;
    var date = moment.unix(news.date).format("DD/MM/YYYY");
    return (
      <Modal show={this.props.show} onHide={this.props.onModalClose}>
        <Modal.Header closeButton>
          <Modal.Title>Notícia: Confirme apenas se os dados estiverem corretos...</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Grid>
            <Row>
              <Col md={10}>
                <form onSubmit={this.handleSubmit}>
                  <FormControls.Static label="id" value={news.id} />

                  <FormControls.Static label="Título" value={news.title} />
                  <FormControls.Static label="Url" value={news.url} />
                  <FormControls.Static label="Data" value={date} />
                  <FormControls.Static label="Publicado" value={news.published + ""} />
                  <FormControls.Static label="TLDR;" value={news.tldr} />
                  <FormControls.Static label="Enviar notificação pro app?" value={locBool(news.sendAppNotification)} />
                  <FormControls.Static label="Mensagem da notificação" value={news.appMessage} />

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

          <ButtonInput onClick={this.props.onModalClose}>Fechar</ButtonInput>
        </Modal.Footer>
      </Modal>
    );
  }
});


var ImportantNews = React.createClass({
  getInitialState: function() {
    return $.extend(this.props.news, {showPanel: false})
  },
  handleTitleChange: function(e) {
    this.setState({ title: e.target.value })
  },
  handleUrlChange: function(e) {
    this.setState({ url: e.target.value })
  },
  handlePublishSelect: function(e) {
    this.setState({ published: e.target.value })
  },
  handleDateChange: function(e) {
    this.setState({ date: e.unix() })
  },
  handleTldrChange: function(e) {
    this.setState({ tldr: e.target.value })
  },
  handleAppNotificationSelect: function(e) {
    this.setState({ sendAppNotification: e.target.value })
  },
  handleAppMessageChange: function(e) {
    this.setState({ appMessage: e.target.value })
  },
  handlePanelHeadClick: function(e) {
    this.setState({ showPanel: !this.state.showPanel })
  },
  handleSubmit: function(e) {
    e.preventDefault();
    var stateCopy = $.extend({}, this.state);
    delete stateCopy.sendAppNotification;
    delete stateCopy.appMessage;
    delete stateCopy.usersId;
    delete stateCopy.timeCreated;
    this.props.onUpdateNewsRequest(stateCopy);
  },
  render: function() {
    var panelClassName = "panel " + (this.state.published ? "panel-success" : "panel-danger");
    var panelBodyClassName = "panel-body " + (this.state.showPanel ? "" : "collapse");
    return (
      <Row>
        <Col md={12}>
          <div className={panelClassName}>
            <div className="panel-heading" onClick={this.handlePanelHeadClick}>
              <h3 className="panel-title"><small>notícia</small> {this.state.title}</h3>
            </div>
            <div className={panelBodyClassName}>
              <form onSubmit={this.handleSubmit}>
                <Input type="textarea"
                  value={this.state.title}
                  placeholder="Título..."
                  label="Título"
                  onChange={this.handleTitleChange}/>

                <Input type="textarea"
                  value={this.state.url}
                  label="Link (url)"
                  placeholder="http://..."
                  onChange={this.handleUrlChange}/>

                <Input type="select" label="Publicado" value={this.state.published} onChange={this.handlePublishSelect}>
                  <option value="true">Sim</option>
                  <option value="false">Não</option>
                </Input>

                <div className="form-group">
                  <label>Data da Notícia</label>
                  <DatePicker
                    dateFormat="DD/MM/YYYY"
                    selected={moment.unix(this.state.date) }
                    onChange={this.handleDateChange} />
                </div>


                <Input type="textarea"
                  value={this.state.tldr}
                  placeholder="Era uma vez..."
                  label="TLDR;"
                  help="Resumo da notícia (é opcional)"
                  onChange={this.handleTldrChange}/>

                <FormControls.Static label="Foi enviada notificação pro app?" value={locBool(this.state.sendAppNotification)}/>
                <FormControls.Static label="Mensagem da notificação" value={this.state.appMessage}/>

                <ButtonInput type="submit" className="btn-primary">Atualizar notícia</ButtonInput>
              </form>
            </div>
          </div>
        </Col>
      </Row>
    )
  }
});

var ImportantNewsEmpty = React.createClass({
  getInitialState: function() {
    return {
      title: "",
      url: "",
      published: true,
      date: moment().unix(),
      tldr: "",
      appMessage: "",
      sendAppNotification: false,
      showModal: false
    }
  },
  handleTitleChange: function(e) {
    this.setState({ title: e.target.value })
  },
  handleUrlChange: function(e) {
    this.setState({ url: e.target.value })
  },
  handlePublishSelect: function(e) {
    this.setState({ published: e.target.value })
  },
  handleDateChange: function(e) {
    this.setState({ date: e.unix() })
  },
  handleTldrChange: function(e) {
    this.setState({ tldr: e.target.value })
  },
  handleAppNotificationSelect: function(e) {
    this.setState({ sendAppNotification: e.target.value })
  },
  handleAppMessageChange: function(e) {
    this.setState({ appMessage: e.target.value })
  },
  handleSubmit: function(e) {
    var stateCopy = $.extend({}, this.state);
    delete stateCopy.showModal;
    this.props.onNewNewsRequest(stateCopy);
  },
  handleModalClose: function() {
    this.setState({ showModal: false })
  },
  handleShowModal: function(e) {
    e.preventDefault();
    this.setState({ showModal: true })
  },
  render: function() {
    return (
      <Row>
        <Col md={12}>
          <ImportantNewsModal show={this.state.showModal} onModalClose={this.handleModalClose} onNewsSubmit={this.handleSubmit} data={this.state} />
          <div className="panel panel-info">
            <div className="panel-heading">
              <h3 className="panel-title">Criar nova notícia</h3>
            </div>
            <div className="panel-body">
              <form onSubmit={this.handleShowModal}>
                <Input type="textarea"
                  value={this.state.title}
                  placeholder="Título..."
                  label="Título"
                  onChange={this.handleTitleChange}/>

                <Input type="textarea"
                  value={this.state.url}
                  label="Link (url)"
                  placeholder="http://..."
                  onChange={this.handleUrlChange}/>

                <Input type="select" label="Publicado" value={this.state.published} onChange={this.handlePublishSelect}>
                  <option value="true">Sim</option>
                  <option value="false">Não</option>
                </Input>

                <div className="form-group">
                  <label>Data da Notícia</label>
                  <DatePicker
                    dateFormat="DD/MM/YYYY"
                    selected={moment.unix(this.state.date) }
                    onChange={this.handleDateChange} />
                </div>


                <Input type="textarea"
                  value={this.state.tldr}
                  placeholder="Era uma vez..."
                  label="TLDR;"
                  help="Resumo da notícia (é opcional)"
                  onChange={this.handleTldrChange}/>

                <Input type="select"
                  label="Enviar App Notificação?"
                  help="Se sim, TODOS os usuários da aplicação vão receber uma notificação! Cuidado ;)"
                  value={this.state.sendAppNotification}
                  onChange={this.handleAppNotificationSelect}>
                  <option value="true">Sim</option>
                  <option value="false">Não</option>
                </Input>

                <Input type="textarea"
                  value={this.state.appMessage}
                  label="Mensagem da Notificação"
                  help="Mensagem que será usada na notificação. Será usada apenas caso tenha escolhido 'SIM' acima. (mantenha curto)"
                  onChange={this.handleAppMessageChange}/>

                <ButtonInput type="submit" className="btn-danger">Criar notícia</ButtonInput>
              </form>
            </div>
          </div>
        </Col>
      </Row>
    )
  }
});

var ImportantNewsList = React.createClass({
  render: function() {
    var onUpdateNewsRequest = this.props.onUpdateNewsRequest;
    var showNotPublishedItems = this.props.showNotPublishedItems;
    var list = this.props.data.map(function(news) {
      if (showNotPublishedItems || news.published) {
        return (
          <ImportantNews key={news.id} news={news} onUpdateNewsRequest={onUpdateNewsRequest} />
        )
      } else {
        return <div key={news.id}></div>
      }
    }
    );
    return (
      <div>
        <Row className="show-grid">
          <Col md={12}>
            <div className="page-header">
              <h1>Notícias importantes</h1>
            </div>
          </Col>
        </Row>
        {list}
      </div>
    )
  }
});

var ImportantNewsRepository = React.createClass({
  getInitialState: function() {
    return { data: [] }
  },
  loadNewsFromServer: function() {
    restCall(
      this.props.serverPath + "importantNews/editor",
      'GET',
      this.props.credential,
      null,
      function(data) {
        this.setState({ data: data })
      }.bind(this),
      function(xhr, status, err) {
        console.error(err, status, err.toString());
        sweetAlert("Erro", status + ", " + xhr.responseText);
      }
    )
  },

  handleNewNewsRequest: function(news) {
    restCall(
      this.props.serverPath + "importantNews/",
      'POST',
      this.props.credential,
      news,
      function(data) {
        sweetAlert("Sucesso", JSON.stringify(data, null, 2));
        this.loadNewsFromServer();
      }.bind(this),
      function(xhr, status, err) {
        console.error(err, status, err.toString());
        sweetAlert("Erro", status + ", " + xhr.responseText);
      }
    )
  },

  handleUpdateNewsRequest: function(news) {
    restCall(
      this.props.serverPath + "importantNews/" + news.id,
      'PUT',
      this.props.credential,
      news,
      function(data) {
        sweetAlert("Sucesso", JSON.stringify(data, null, 2));
        this.loadNewsFromServer();
      }.bind(this),
      function(xhr, status, err) {
        console.error(err, status, err.toString());
        sweetAlert("Erro", status + ", " + xhr.responseText);
      }
    )
  },

  render: function() {
    return (
      <div>
        <ImportantNewsList key={moment().valueOf() } data={this.state.data} onUpdateNewsRequest={this.handleUpdateNewsRequest}
          showNotPublishedItems={this.props.showNotPublishedItems} />
        <ImportantNewsEmpty onNewNewsRequest={this.handleNewNewsRequest} />
      </div>
    )
  }
});


module.exports = {
  ImportantNewsRepository: ImportantNewsRepository
}