import React from "react"
import {restRequest} from "../util/RestManager.js"
import "../../styles/setuppages/setuppages.css"

export class SetupPage1 extends React.Component{

    constructor(props){
        super(props);
        this.state = {installKafka: true, buttonText: "installiere", progress: 0, kafkaPath: "", kafkaIsInstalling: false}
    }

    onRequestSuccess = (responseText) => {
        console.log(responseText.state);
    }

    onError = (responseCode, responseText) => {
        if (this.intervalID != null){
            clearInterval(this.intervalID);
            this.interval = null;
        }
        console.log(JSON.parse(responseText).message)
    }

    installKafka(){
        restRequest("/greenlake-platform/setup/installKafka", "PUT", "{}", this.onRequestSuccess, this.onError);
    }

    handleRadioButtonChange = (changeEvent) => {
        if (changeEvent.target.value == "installKafka"){
            this.setState({installKafka: true, buttonText: "installiere"});
        } else {
            this.setState({installKafka: false, buttonText: "weiter"});
        }
    }

    onButtonClick = () => {
        if (this.state.installKafka){
            if (this.state.kafkaPath != ""){
                this.installKafka();
                this.intervalID = setInterval(
                    () => this.requestProgress(),
                    500
                );
            }
        }
    }

    requestProgress = () => {
        this.setState({kafkaIsInstalling: true});
        restRequest("/greenlake-platform/setup/kafkaSetupProgress", "GET", "{}", this.updateProgress, this.onError);
    }

    updateProgress = (responseText) => {
        var kafkaSetupProgress = JSON.parse(responseText).progress;

        if (kafkaSetupProgress == 100){
            clearInterval(this.intervalID);
            this.setState({installKafka: false, buttonText: "weiter"});
        }

        this.setState({progress: kafkaSetupProgress});
    }

    setKafkaPath = (event) => {
        this.setState({kafkaPath: event.target.value});
    }

    render(){
        return(
            <div>
                <p style={{width: "100%", textAlign: "center", fontSize: "1.5em"}}>Kafka Installation</p>
                <form style={{margin: "0px"}}>
                  <input type="radio" id="installKafka" name="installKafka" value="installKafka" onChange={this.handleRadioButtonChange} checked={this.state.installKafka}/>
                  <label htmlFor="installKafka">Installiere Kafka</label><br />
                  <input type="radio" id="kafkaAlreadyInstalled" name="installKafka" value="kafkaAlreadyInstalled" onChange={this.handleRadioButtonChange} checked={!this.state.installKafka}/>
                  <label htmlFor="kafkaAlreadyInstalled">Kafka ist bereits installiert</label><br /><br />

                  <label htmlFor="kafkaInstallPath">Pfad: </label>
                  <input style={{width: "82%"}} name="kafkaInstallPath" id="kafkaInstallPath" required={true} onChange={this.setKafkaPath}/>
                </form>

                <div style={{visibility: this.state.installKafka ? "visible" : "hidden"}}>
                    <label htmlFor="downloadProgress">Fortschritt: </label>
                    <progress style={{width: "73%"}} id="downloadProgress" name="downloadProgress" value={this.state.progress} max="100" />
                    <p style={{display: "inline-block", marginLeft:"5px"}}>{this.state.progress}%</p>
                </div>
                <button type="button" style={{position: "absolute", right: "10px", bottom: "10px"}} onClick={this.onButtonClick}>{this.state.buttonText}</button>
            </div>
        );
    }

}