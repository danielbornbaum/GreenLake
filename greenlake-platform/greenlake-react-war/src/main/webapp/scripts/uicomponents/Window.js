import React from "react"
import Draggable from "react-draggable"
import "../../styles/uicomponents/window.css"

export class Window extends React.Component{

    constructor(props){
        super(props);

        var pos_x = (window.innerWidth/2 - (this.props.width/2)).toString().concat("px");
        var pos_y = (window.innerHeight/2 - (this.props.height/2)).toString().concat("px");

        this.style = {
            width: this.props.width,
            height: this.props.height,
            top: pos_y,
            left: pos_x,
            backgroundColor: "#FFF",
            overflow: "hidden"
        };

        this.state = {hidden: false};
    }

    onCloseButton = () => {
        this.setState({hidden: true});
    }

    render(){
        return(
            <div>
                <Draggable>
                    <div className="window" style={this.style}>
                        <div className="window-title">{this.props.title} <div className="x-button-container" onClick={this.onCloseButton}><div className="x-button" /></div></div>
                        <div className="window-content">{this.props.children}</div>
                    </div>
                </Draggable>
            </div>
        );
    }

}