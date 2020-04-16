import React from "react"
import ReactDOM from "react-dom"
import {MenuButton} from "./uicomponents/MenuButton.js"
import {Window} from "./uicomponents/Window.js"
import {restRequest} from "./util/RestManager.js"
import {SetupPage1} from "./setuppages/SetupPage1.js"

class App extends React.Component{

    constructor(props) {
        super(props);
        this.state = {time: new Date().toLocaleTimeString(), title:"Home", hidden:true};
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

    onError = (responseCode, responseText) => {

    }

    onSetupRequestSuccess = (responseText) => {
        this.setState({hidden: JSON.parse(responseText).performed});
    }

    checkForSetup() {
        restRequest("/greenlake-platform/setup/status", "GET", "{}", this.onSetupRequestSuccess, this.onError);
    }

    render(){
        return(
            <div>
                <div id="menubar">
                    <MenuButton />
                    <p id="pagetitle">{this.state.title}</p>
                    <p id="clock">{this.state.time}</p>
                </div>
                <div id="application-body">
                    <Window width={500} height={300} title="Setup" style={{visibility: this.state.hidden ? "hidden" : "visible"}}>
                        <SetupPage1 />
                    </Window>
                </div>
            </div>
        )
    }
}

ReactDOM.render(<App />, document.getElementById("app"));