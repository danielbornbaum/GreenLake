import React from "react"
import ReactDOM from "react-dom"
import "../../styles/components/new-topic-formula.css"
import {restRequest} from "../util/RestManager.js"

export class NewTopicFormula extends React.Component{

    constructor(props){
        super(props);
        this.state = {name: "", partitions: 1, replications: 1};
    }

    onSubmit = () => {
        if (this.props.submitCommand != null){
            this.props.submitCommand();
        }

        var parameters = {name: this.state.name, partitions: this.state.partitions,
                          replications: this.state.replications};

        restRequest("/kafka-configuration-server/kafka/addTopic", "POST", JSON.stringify(parameters),
                    (resultText) => {});
    }

    render(){
        return(
            <form>
                <label htmlFor="topicNameInput">Name</label>
                <input name="topicNameInput" required={true} value={this.state.name}
                 onChange={(event) => this.setState({name: event.target.value})}/><br />
                <label htmlFor="topicPartitionsInput">Partitionen</label>
                <input type="number" min={1} name="topicPartitionsInput" required={true} value={this.state.partitions}
                 onChange={(event) => this.setState({partitions: event.target.value})}/><br />
                <label htmlFor="topicReplicationsInput">Replikationen</label>
                <input type="number" min={1} name="topicReplicationsInput" required={true}
                 value={this.state.replications} onChange={(event) =>
                 this.setState({replications: event.target.value})}/><br />
                <input type="submit" value="anlegen" onClick={this.onSubmit}/>
            </form>
        );
    }
}