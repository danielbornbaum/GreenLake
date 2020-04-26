import React from "react"
import {restRequest} from "../util/RestManager.js"
import "../../styles/setuppages/setuppages.css"

export class SetupPage1 extends React.Component{

    constructor(props){
        super(props);
        this.state = {installKafka: true, buttonText: "installiere", progress: 0, kafkaPath: "", kafkaIsInstalling: false,
            buttonEnabled: true}
    }

    onInstallKafkaRequestSuccess = (responseText) => {
        clearInterval(this.intervalID);
        this.setState({kafkaIsInstalling: false, buttonEnabled: true, buttonText: "weiter", installKafka: false,
                       progress: 0});
    }

    onInstallKafkaRequestError = (responseText) => {
        clearInterval(this.intervalID);
        this.setState({kafkaIsInstalling: false, buttonEnabled: true, buttonText: "weiter", installKafka: true,
                       progress: 0});
    }

    onKafkaVerifyRequestSuccess = (responseText) => {
        if (JSON.parse(responseText).present){
            // this.props.nextAction(this.props.nextPageIndex);
        } else {
            alert("There is no valid Kafka installation at that location.");
        }
    }

    installKafka(){
        this.setState({kafkaIsInstalling: true, buttonEnabled: false});
        restRequest("/greenlake-platform/setup/installKafka", "PUT", JSON.stringify({path: this.state.kafkaPath}),
                    this.onInstallKafkaRequestSuccess , this.onInstallKafkaRequestError);
    }

    verifyKafkaAtLocation(){
        restRequest("/greenlake-platform/setup/validateKafkaAtLocation", "POST",
            JSON.stringify({path: this.state.kafkaPath}), this.onKafkaVerifyRequestSuccess)
    }

    handleRadioButtonChange = (changeEvent) => {
        if (changeEvent.target.value == "installKafka"){
            this.setState({installKafka: true, buttonText: "installiere"});
        } else {
            this.setState({installKafka: false, buttonText: "weiter"});
        }
    }

    onButtonClick = () => {
        if (this.state.kafkaPath == ""){
            alert("Bitte Kafka-Pfad setzen");
            return;
        }

        if (this.state.installKafka){
            if (!this.state.kafkaIsInstalling){
                this.installKafka();
                this.intervalID = setInterval(
                    () => this.requestProgress(),
                    500
                );
            }
        } else {
            this.verifyKafkaAtLocation();
        }
    }

    requestProgress = () => {
        restRequest("/greenlake-platform/setup/kafkaSetupProgress", "GET", "{}", this.updateProgress);
    }

    updateProgress = (responseText) => {
        var kafkaSetupProgress = JSON.parse(responseText).progress;
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
                  <input type="radio" id="installKafka" name="installKafka" value="installKafka"
                    onChange={this.handleRadioButtonChange} checked={this.state.installKafka}/>
                  <label htmlFor="installKafka">Installiere Kafka¹</label><br />
                  <input type="radio" id="kafkaAlreadyInstalled" name="installKafka" value="kafkaAlreadyInstalled"
                    onChange={this.handleRadioButtonChange} checked={!this.state.installKafka}/>
                  <label htmlFor="kafkaAlreadyInstalled">Kafka ist bereits installiert</label><br /><br />

                  <label htmlFor="kafkaInstallPath">Pfad: </label>
                  <input style={{width: "82%"}} name="kafkaInstallPath" id="kafkaInstallPath" required={true}
                    onChange={this.setKafkaPath}/>
                </form>

                {this.state.installKafka &&
                 <div>
                    <label htmlFor="downloadProgress">Fortschritt: </label>
                    <progress style={{width: "73%"}} id="downloadProgress" name="downloadProgress" value={this.state.progress} max="100" />
                    <p style={{display: "inline-block", marginLeft:"5px"}}>{this.state.progress}%</p>
                </div>
                }
                <p style={{fontSize: "6pt", maxWidth:"80%", position: "absolute", left: "10px",
                    bottom: "10px"}}>
                    ¹ Installing Apache Kafka is the equivalent of downloading and untaring Kafka from the official
                    website, hence you agree to the Apache License 2.0.
                </p>
                <button type="button" disabled={!this.state.buttonEnabled} style={{position: "absolute", right: "10px",
                    bottom: "10px", maxWidth:"66px"}} onClick={this.onButtonClick}>{this.state.buttonText}</button>
            </div>
        );
    }

}