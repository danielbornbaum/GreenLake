import React from "react"
import ReactDOM from "react-dom"
import {MenuButton} from "./uicomponents/MenuButton.js"

class App extends React.Component{

    constructor(props) {
        super(props);
        this.state = {time: new Date().toLocaleTimeString(), title:"Home"};
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

    //                    <div>{new MenuButton()}</div>

    render(){
        return(
            <div>
                <div id="menubar">
                    <MenuButton />
                    <p id="pagetitle">{this.state.title}</p>
                    <p id="clock">{this.state.time}</p>
                </div>
                <div id="application-body">
                </div>
            </div>
        )
    }
}

ReactDOM.render(<App />, document.getElementById("app"));