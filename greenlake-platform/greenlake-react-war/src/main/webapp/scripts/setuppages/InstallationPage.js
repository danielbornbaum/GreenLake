import React from "react"
import {restRequest} from "../util/RestManager.js"
import "../../styles/setuppages/setuppages.css"
import {SetupPageFrame} from "./SetupPageFrame.js"

export class InstallationPage extends React.Component{

    constructor(props){
        super(props);

        var previousState = sessionStorage.getItem(props.id+"_State");

        if (previousState == null)
        {
            this.state = {install: true, buttonText: "installiere", progress: 0, path: "", isInstalling: false,
                buttonEnabled: true}
        } else {
            this.state = JSON.parse(previousState);
        }
    }

    onInstallRequestSuccess = (responseText) => {
        clearInterval(this.intervalID);
        this.setState({isInstalling: false, buttonEnabled: true, buttonText: this.props.nextText, install: false,
                       progress: 0});
    }

    onInstallRequestError = (responseText) => {
        clearInterval(this.intervalID);
        this.setState({isInstalling: false, buttonEnabled: true, buttonText: "installiere", install: true,
                       progress: 0});
    }

    onVerifyRequestSuccess = (responseText) => {
        if (JSON.parse(responseText).present){
            sessionStorage.setItem(this.props.id+"_State", JSON.stringify(this.state));
            this.props.nextAction();
        } else {
            alert("Keine valide Installation am angegebenen Pfad gefunden");
        }
    }

    install(){
        this.setState({isInstalling: true, buttonEnabled: false});
        restRequest(this.props.installationURL, "PUT", JSON.stringify({path: this.state.path}),
                    this.onInstallRequestSuccess , this.onInstallRequestError);
    }

    verifyInstallationAtLocation(){
        restRequest(this.props.verificationURL, "POST", JSON.stringify({path: this.state.path}),
                    this.onVerifyRequestSuccess)
    }

    handleRadioButtonChange = (changeEvent) => {
        if (changeEvent.target.value == "install"){
            this.setState({install: true, buttonText: "installiere"});
        } else {
            this.setState({install: false, buttonText: this.props.nextText});
        }
    }

    onNextButtonClick = () => {
        if (this.state.path == ""){
            alert("Bitte einen Pfad setzen");
            return;
        }

        if (this.state.install){
            if (!this.state.isInstalling){
                this.install();
                this.intervalID = setInterval(
                    () => this.requestProgress(),
                    500
                );
            }
        } else {
            this.verifyInstallationAtLocation();
        }
    }

    requestProgress = () => {
        restRequest(this.props.progressURL, "GET", "{}", this.updateProgress);
    }

    updateProgress = (responseText) => {
        var setupProgress = JSON.parse(responseText).progress;
        this.setState({progress: setupProgress});
    }

    setPath = (event) => {
        this.setState({path: event.target.value});
    }

    render(){
        return(
            <div style={{maxHeight:"100%"}}>
                <SetupPageFrame nextButtonEnabled={this.state.buttonEnabled} previousAction={this.props.previousAction}
                 nextAction={this.onNextButtonClick} title={this.props.installationGoal+" Installation"}
                 nextText={this.state.buttonText}>
                    <form style={{margin: "0px"}}>
                        <input type="radio" id="install" name="install" value="install"
                         onChange={this.handleRadioButtonChange} checked={this.state.install}/>
                        <label htmlFor="install">Installiere {this.props.installationGoal}¹</label><br />
                        <input type="radio" id="alreadyInstalled" name="install" value="alreadyInstalled"
                         onChange={this.handleRadioButtonChange} checked={!this.state.install}/>
                        <label htmlFor="alreadyInstalled">{this.props.installationGoal} ist bereits installiert</label><br /><br />

                        <label htmlFor="kafkaInstallPath">Pfad: </label>
                        <input style={{width: "82%"}} name="installPath" id="installPath" required={true}
                         onChange={this.setPath} defaultValue={this.state.path}/>
                    </form>

                    {this.state.install &&
                        <div>
                            <label htmlFor="downloadProgress">Fortschritt: </label>
                            <progress style={{width: "73%"}} id="downloadProgress" name="downloadProgress"
                             value={this.state.progress} max="100" />
                            <p style={{display: "inline-block", marginLeft:"5px"}}>{this.state.progress}%</p>
                        </div>
                    }
                </SetupPageFrame>
            </div>
        );
    }

}