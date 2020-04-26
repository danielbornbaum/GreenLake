import React from "react";
import "../../styles/uicomponents/main-menu-item.css"

export class MainMenuItem extends React.Component{
    constructor(props){
        super(props);
    }

    onClickCommand = (event) => {
        this.props.contentUrlCommand(this.props.url, this.props.title)
    }

    render(){
        return(
            <div className="menuItem" onClick={this.onClickCommand}>
                <div>
                    <img src={this.props.icon} />
                    <p>{this.props.title}</p>
                </div>
                <hr style={{margin: "0px", padding:"0px"}} />
            </div>
        );
    }
}