import React from "react"
import {restRequest} from "../util/RestManager.js"
import "../../styles/setuppages/setuppages.css"

export class SetupPageFrame extends React.Component{

    constructor(props){
        super(props);
        this.state = {nextButtonEnabled: true, previousButtonEnabled:true}
    }

    onNextButtonClick = () => {
        this.setState({nextButtonEnabled: false});
        this.props.nextAction();
        this.setState({nextButtonEnabled: true});
    }

    onPreviousButtonClick = () => {
        this.setState({previousButtonEnabled: false});
        this.props.previousAction();
        this.setState({previousButtonEnabled: true});
    }

    render(){
        return(
            <div style={{display: "flex", flexDirection: "column", maxHeight:"100%", height:"100%"}}>
                <p style={{width: "100%", textAlign: "center", fontSize: "1.5em", marginTop: 0}}>
                    {this.props.title}
                </p>
                <div style={{flex: 1, overflowY: "auto", marginBottom: "5px"}}>
                    {this.props.children}
                </div>
                <div style={{display: "flex", justifyContent: "space-between"}}>
                    {this.props.previousAction == null && <p></p>}
                    {this.props.previousAction != null && <button type="button"
                     disabled={!this.state.previousButtonEnabled} onClick={this.onPreviousButtonClick}>zurück</button>}

                    <button onClick={this.onNextButtonClick} disabled={!this.props.nextButtonEnabled
                                                                       || !this.state.nextButtonEnabled}>
                        {this.props.nextText}
                    </button>
                </div>
            </div>
        );
    }

}