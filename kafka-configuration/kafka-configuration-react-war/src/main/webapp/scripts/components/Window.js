import React from "react"
import Draggable from "react-draggable"
import "../../styles/components/window.css"

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
                            {this.props.closable &&
                                <div className="x-button-container" onClick={this.props.closeCommand}>
                                    <div className="x-button" onClick={this.props.closeCommand}/>
                                </div>
                            }
                        </span>
                        <div className="window-content" draggable>{this.props.children}</div>
                    </div>
                </Draggable>
            </div>
        );
    }

}