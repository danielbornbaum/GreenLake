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
            maxWidth: "100%",
            height: this.props.height,
            top: pos_y,
            left: pos_x,
            backgroundColor: "#FFF",
            overflow: "hidden"
        };

        this.state = {hidden: false};
    }

    render(){
        return(
            <div style={{position: "absolute", top: 0, left: 0, width: "100%", height:"100%",
                         backgroundColor: "rgba(150, 150, 150, 0.4)", visibility: this.props.visibility}}>
                <Draggable handle="span">
                    <div className="window" style={this.style}>
                        <span className="window-title">{this.props.title}
                            <div className="x-button-container" onClick={this.props.closeCommand}>
                                <div className="x-button" onClick={this.props.closeCommand}/>
                            </div>
                        </span>
                        <div className="window-content" draggable
                         onDragStart={e => {e.preventDefault(); e.stopPropagation();}} >{this.props.children}</div>
                    </div>
                </Draggable>
            </div>
        );
    }

}