import React from "react"
import ReactDOM from "react-dom"
import "../styles/kafka-configuration.css"
import {restRequest} from "./util/RestManager.js"
import {Window} from "./components/Window.js"
import {NewTopicFormula} from "./components/NewTopicFormula.js"

class App extends React.Component{

    constructor(props){
        super(props);
        this.state = {topicToDelete: null, topics: [], kafkaActive: false, zookeeperActive: false,
                      topicWindowVisible: false};
    }

    componentDidMount() {
        this.intervalID = setInterval(
            () => this.requestKafkaAndZookeeperStatus(),
            3000
        );
    }

    requestKafkaAndZookeeperStatus(){
        restRequest("/kafka-configuration-server/kafka/kafkaAvailable", "GET", "", this.onKafkaAvailableRequestSuccess);
        restRequest("/kafka-configuration-server/kafka/zookeeperAvailable", "GET", "",
                    this.onZookeeperAvailableRequestSuccess);
    }

    onKafkaAvailableRequestSuccess = (responseText) => {
        var kafkaAvailable = JSON.parse(responseText).available;
        this.setState({kafkaActive: kafkaAvailable});

        if (kafkaAvailable){
            restRequest("/kafka-configuration-server/kafka/listTopics", "GET", "",
                        this.onListTopicsSuccess);
        }
    }

    onZookeeperAvailableRequestSuccess = (responseText) => {
        this.setState({zookeeperActive: JSON.parse(responseText).available});
    }

    toggleTopicWindowVisible = () => {
        this.setState({topicWindowVisible: !this.state.topicWindowVisible});
    }

    onListTopicsSuccess = (responseText) => {
        this.setState({topics: JSON.parse(responseText).topics})
    }

    askDeleteTopic = (name) => {
        this.setState({topicToDelete: name});
    }

    clearTopicToDelete = () => {
        this.setState({topicToDelete: null});
    }

    deleteTopicToDelete = () => {
        restRequest("/kafka-configuration-server/kafka/deleteTopic", "DELETE",
                    JSON.stringify({name: this.state.topicToDelete}));
        this.setState({topicToDelete: null});
    }

    render(){
        return(
            <div className="kafka-config-container">

                <div className="kafka-titlebar">
                    <p>Kafka Status:</p>
                    <p className="activeSwitch" style={{backgroundColor: this.state.kafkaActive ? "#32a63b" : "#bababa",
                     boxShadow: this.state.kafkaActive ? "0px 0px 13px 0px #0DFF66" : "none"}}>
                        {this.state.kafkaActive ? "Aktiv" : "Inaktiv"}
                    </p>
                    <p>Zookeeper Status:</p>
                    <p className="activeSwitch" style={{backgroundColor: this.state.zookeeperActive ? "#32a63b" : "#bababa",
                     boxShadow: this.state.kafkaActive ? "0px 0px 13px 0px #0DFF66" : "none"}}>
                        {this.state.zookeeperActive ? "Aktiv" : "Inaktiv"}
                    </p>
                </div>

                <div className="topics">

                    <button className="topic-add-button" disabled={!(this.state.kafkaActive && this.state.zookeeperActive)}
                     onClick={this.toggleTopicWindowVisible}>+</button>

                    <div className="topics-content-div">
                        <div>
                            <p className="title-element">Topics</p>
                            <ul>
                                {this.state.topics.map(topic => <li key={topic.name}>{topic.name}</li>)}
                            </ul>
                        </div>
                        <div>
                            <p className="title-element">Partitionen</p>
                            <ul>
                                {this.state.topics.map(topic => <li key={topic.name}>{topic.partitions}</li>)}
                            </ul>
                        </div>
                        <div>
                            <p className="title-element">Replikationen</p>
                            <ul>
                                {this.state.topics.map(topic => <li key={topic.name}>{topic.replications}</li>)}
                            </ul>
                        </div>
                    </div>
                </div>

                {this.state.topicWindowVisible &&
                 <Window width={500} height={180} title="Neues Topic anlegen"
                  closeCommand={this.toggleTopicWindowVisible} closable={true}>
                    <NewTopicFormula submitCommand={this.toggleTopicWindowVisible}/>
                 </Window>}
            </div>
        )
    }
}

ReactDOM.render(<App />, document.getElementById("app"));