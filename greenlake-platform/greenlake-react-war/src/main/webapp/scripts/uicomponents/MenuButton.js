import React from "react"
import "../../styles/uicomponents/menu-button.css"

export class MenuButton extends React.Component{
    render(){
        return(
            <div id="menubutton" className={this.props.open ? "opened" : ""} onClick={this.props.openMenuCommand}>
                <div className="menubutton-burger"></div>
            </div>
        )
    }
}