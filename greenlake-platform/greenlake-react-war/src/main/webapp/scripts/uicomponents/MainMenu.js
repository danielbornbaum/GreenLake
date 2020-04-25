import React from "react"
import {restRequest} from "../util/RestManager.js"
import {MainMenuItem} from "./MainMenuItem.js"

export class MainMenu extends React.Component{

    constructor(props){
        super(props);

        this.state = {apps: []}
        restRequest("/greenlake-platform/apps/get", "GET", "", this.onAppRequestSuccess);
    }

    onAppRequestSuccess = (responseText) =>{
        var appList = JSON.parse(responseText).apps;
        this.setState({apps: appList});
    }

    render(){
        return(
            <div className={this.props.visibility ? "side-menu-fade-in" : "side-menu-fade-out"}>
                {this.state.apps.map(app => <MainMenuItem key={app.appId} contentUrlCommand={this.props.contentUrlCommand}
                 title={app.name} icon={app.iconPath} url={app.url}/>)}
            </div>
        )
    }
}