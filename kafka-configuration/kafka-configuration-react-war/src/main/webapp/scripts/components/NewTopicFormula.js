import React from "react"
import ReactDOM from "react-dom"
import "../../styles/components/new-topic-formula.css"

export class NewTopicFormula extends React.Component{
    render(){
        return(
            <formula>
                <label htmlFor="topicNameInput">Name</label>
                <input name="topicNameInput" required={true} /><br />
                <label htmlFor="topicPartitionsInput">Partitionen</label>
                <input type="number" name="topicPartitionsInput" required={true} /><br />
                <label htmlFor="topicReplicationsInput">Replikationen</label>
                <input type="number" name="topicReplicationsInput" required={true} /><br />
                <input type="submit" value="anlegen"/>
            </formula>
        );
    }
}