import React from "react"
import {restRequest} from "../util/RestManager.js"
import "../../styles/setuppages/setuppages.css"
import {SetupPageFrame} from "./SetupPageFrame.js"

export class ConfigurationPage extends React.Component
{
    constructor(props)
    {
        super(props);

        var previousState = sessionStorage.getItem(props.id+"_State");

        if (previousState == null){
            var properties = {}

            this.props.properties.forEach(property =>
            {
                if (property.type !== "group")
                {
                    properties[property.name] = property.defaultValue;
                }
            });

            this.state = {properties : properties};
        }
        else
        {
            this.state = JSON.parse(previousState);
        }
    }

    onNextButtonClick = () =>
    {
        Object.keys(this.state.properties).forEach(key =>
        {
            if (this.state.properties[key] === "")
            {
                alert("Bitte zuerst alle Einstellungen setzen");
                return;
            }
         });

        sessionStorage.setItem(this.props.id+"_State", JSON.stringify(this.state));
        restRequest(this.props.propertiesUrl, "POST", JSON.stringify({config: this.state.properties}),
                    this.onPropertiesSuccess);
    }

    onPreviousButtonClick = () =>
    {
        this.props.previousAction();
    }

    onPropertiesSuccess = (textResponse) => {
        this.props.nextAction();
    }

    setProperty = (key, value) =>
    {
        var values = this.state.properties;
        values[key] = value;
        this.setState({properties : values});
    }

    render(){
        return(
            <div style={{maxHeight:"100%"}}>
                <SetupPageFrame nextButtonEnabled={true} previousAction={this.props.previousAction}
                 nextAction={this.onNextButtonClick} title={this.props.configurationGoal+" Konfiguration"}
                 nextText={this.props.nextText}>
                    <div>
                            {
                                this.props.properties.map(property =>
                                {
                                    var propertyValue = this.state.properties[property.name];

                                    if (property.type === "group"){
                                        return(<p key={property.name}><b>{property.label}</b></p>);
                                    }
                                    else if (property.type === "boolean"){
                                        return(
                                            <div key={property.name} style={{display: "flex",
                                                                         justifyContent: "space-between"}}>
                                                <label htmlFor={property.name}>{property.label}: </label>
                                                <input name={property.name} required={true} type="checkbox"
                                                 defaultValue={propertyValue ? "checked" : ""}
                                                 style={{margin: "2px"}} onChange =
                                                 {event => this.setProperty(property.name, event.target.value)}/>
                                            </div>
                                        );
                                    }
                                    else
                                    {
                                        var inputType = property.type == "num" ? "number" : "";
                                        return(
                                            <div key={property.name} style={{display: "flex",
                                                                         justifyContent: "space-between"}}>
                                                <label htmlFor={property.name}>{property.label}: </label>
                                                <input name={property.name} required={true} type={inputType}
                                                 defaultValue={propertyValue} style={{margin: "2px"}} onChange =
                                                 {event => this.setProperty(property.name, event.target.value)}/>
                                            </div>
                                        );
                                    }
                                })
                            }
                    </div>
                </SetupPageFrame>
            </div>
        );
    }
}