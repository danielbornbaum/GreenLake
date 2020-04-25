import React from "react"
import ReactDOM from "react-dom"
import {MenuButton} from "./uicomponents/MenuButton.js"
import {MainMenu} from "./uicomponents/MainMenu.js"
import {Window} from "./uicomponents/Window.js"
import {restRequest} from "./util/RestManager.js"
import {SetupPage1} from "./setuppages/SetupPage1.js"

class App extends React.Component{

    constructor(props) {
        super(props);
        this.state = {time: new Date().toLocaleTimeString(), title:"Home", setupHidden:true, menuHidden:true, currentURL: "/homepage.html"};
        this.checkForSetup();
    }

    componentDidMount() {
        this.intervalID = setInterval(
            () => this.setTime(),
            1000
        );
    }

    componentWillUnmount() {
        clearInterval(this.intervalID);
    }

    setTime(){
        this.setState({time: new Date().toLocaleTimeString()});
    }

    onSetupRequestSuccess = (responseText) => {
        this.setState({setupHidden: JSON.parse(responseText).performed});
    }

    checkForSetup() {
        restRequest("/greenlake-platform/setup/status", "GET", "", this.onSetupRequestSuccess);
    }

    openMainMenu = () => {
        this.setState({menuHidden: !this.state.menuHidden});
    }

    closeSetup = () => {
        this.setState({setupHidden: true});
    }

    setContentURL = (url, title) => {
        this.setState({currentURL: url, menuHidden: true, title: title})
    }

    render(){
        return(
            <div id="app-container">
                <div id="menubar">
                    <MenuButton open={!this.state.menuHidden} openMenuCommand={this.openMainMenu}/>
                    <p id="pagetitle">{this.state.title}</p>
                    <p id="clock">{this.state.time}</p>
                </div>
                <div id="application-body">
                    <MainMenu contentUrlCommand={this.setContentURL} visibility={this.state.menuHidden}/>
                    <iframe src={this.state.currentURL} style={{border: "0px", width: "100%", height: "100%",
                        overflow: "auto"}} />
                    <Window width={500} height={300} title="Setup" visibility={this.state.setupHidden ? "hidden"
                        : "visible"} closeCommand={this.closeSetup}>
                        <SetupPage1 />
                    </Window>
                </div>
            </div>
        )
    }
}

ReactDOM.render(<App />, document.getElementById("app"));