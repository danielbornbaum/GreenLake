import React from "react"
import AceEditor from 'react-ace';
import ace from 'react-ace'
import "ace-builds/webpack-resolver";
import {restRequest} from "../util/RestManager"

import "../../styles/components/job-configurator.css"

export class JobConfigurator extends React.Component{
    constructor(props){
        super(props);

        var code = props.code == undefined ? "" : props.code;
        var title = props.title == undefined ? "Job" : props.title;
        var forgetting = props.forgetting == undefined ? true : props.forgetting;
        var schedulingTime = props.schedulingTime == undefined ? 1000 : props.schedulingTime;
        var timeout = props.timeout == undefined ? 1000 : props.timeout;
        var minDataSets = props.minDataSets == undefined ? 1 : props.minDataSets;
        var maxDataSets = props.maxDataSets == undefined ? 1 : props.maxDataSets;
        var source = this.props.source == undefined ? "Kafka" : this.props.source;
        var destination = this.props.destination == undefined ? "Kafka" : this.props.destination;
        var topicOrDataIn = this.props.topicOrDataIn == undefined ? "" : this.props.topicOrDataIn;
        var topicOrDataOut = this.props.topicOrDataIn == undefined ? "" : this.props.topicOrDataOut;
        var consumerGroup = this.consumerGroup == undefined ? "consumer-group" : this.props.consumerGroup;

        this.state = {id: this.props.id, code: code, title: title, forgetting: forgetting,
                      schedulingTime: schedulingTime, timeout: timeout, expanded: false, edit: false,
                      minDataSets: minDataSets, maxDataSets: maxDataSets, source: source, destination: destination,
                      topicOrDataIn: topicOrDataIn, topicOrDataOut: topicOrDataOut, consumerGroup: consumerGroup};
    }

    cursorState = false;

    onEditSaveButtonClick = () => {
        if (this.state.edit){
            var requestBody = {id: this.state.id, source: this.state.source, destination: this.state.destination,
                               javascript: this.state.code, minDataSetSize: this.state.minDataSets,
                               maxDataSetSize: this.state.maxDataSets, topicOrDataIn: this.state.topicOrDataIn,
                               topicOrDataOut: this.state.topicOrDataOut, forgetting: this.state.forgetting,
                               timeout: this.state.timeout, schedulingTime: this.state.schedulingTime,
                               consumerGroup: this.state.consumerGroup, title: this.state.title}

            restRequest("/data-processing-server/jobs/alter", "POST", JSON.stringify(requestBody),
                        this.onEditSaveRequestSuccess);
        } else {
            this.setState({edit: !this.state.edit, expanded: true});
        }
    }

    onEditSaveRequestSuccess = (responseText) => {
        this.setState({edit: !this.state.edit, expanded: true});
    }

    onDeleteButtonClick = () => {
        restRequest("/data-processing-server/jobs/remove", "DELETE", JSON.stringify({id: this.state.id}), () => {});
    }

    setCode(value) {
        this.setState({ code: value });
    }

    render(){
        return(
            <div className="job-configurator">
                <div className="job-titlebar" style={this.state.expanded ? {borderRadius: "3px 3px 0 0"} :
                 {borderRadius: "3px"}}>
                    <div className="buttons">
                        <button className="expand-button" onClick={() => this.setState({expanded: !this.state.expanded})}
                         style={{backgroundColor: "rgba(255, 255, 255, 0)", fontSize: "14pt", borderWidth: 0}}>
                            {this.state.expanded ? "▲" : "▼"}
                        </button>
                        <button onClick={this.onDeleteButtonClick}>🗑</button>
                        <button onClick={this.onEditSaveButtonClick}>{this.state.edit ? "💾":"✎"}</button>
                    </div>
                    <input className="job-configurator-title" value={this.state.title}
                     onChange={event => this.setState({title: event.target.value})}/>
                    <div className="title-inputs" disabled={!this.state.edit}>
                        <label htmlFor="schedule-time-input" style={{margin: "auto 5px auto 0"}}>Schedule-Zeit (ms):</label>
                        <input type="number" className="job-titlebar-schedule" name="schedule-time-input" min={100}
                         max={Number.MAX_VALUE} value={this.state.schedulingTime}
                         onChange={event => this.setState({schedulingTime: event.target.value})}
                         style={{marginRight: "5px"}}/>

                        <label htmlFor="timeout-time-input" style={{margin: "auto 5px auto 0"}}>Timout Kafka Consumer(ms):</label>
                        <input type="number" className="job-titlebar-schedule" name="timeout-time-input" min={100}
                         max={Number.MAX_VALUE} value={this.state.timeout}
                         onChange={event => this.setState({scheduleTime: event.target.value})} />
                    </div>
                </div>

                {this.state.expanded &&
                    <div>
                        <p style={{margin: "5px", textDecoration: "none"}}><b>Id: </b>{this.state.id}</p>
                        <div className="job-settings" disabled={!this.state.edit}>
                            <div className="source-destination-chooser">
                                <p><b>Quelle:</b></p>
                                <select value={this.state.source}
                                 onChange={event => this.setState({source: event.target.value})}>
                                    <option>Kafka</option>
                                    <option>Hadoop</option>
                                </select>
                                <input required={true} value={this.state.topicOrDataIn}
                                 onChange={event => this.setState({topicOrDataIn: event.target.value})} />

                                {this.state.source == "Kafka" &&
                                    <div>
                                        <label htmlFor="consumerGroupInput">Consumer group:</label>
                                        <input required={true} value={this.state.consumerGroup}
                                         name="consumerGroupInput"
                                         onChange={event => this.setState({consumerGroup: event.target.value})} />
                                    </div>
                                }
                            </div>

                            <img src="images/icons/nodes.png"/>

                            <div style={{flex: 1, height: "100%", textDecoration: "none", fontSize:"10pt",
                             borderRadius: "2px", border: "1px solid #000", backgroundColor: "#FFF", padding: "5px"}}>
                                <p>var data = [n Werte]</p>
                                <AceEditor mode="javascript" theme="monokai" value={this.state.code}
                                 onChange={value => {this.setCode(value);}}
                                 style={{width: "auto", height: "80px", margin: "2px"}} />
                                <p>return result;</p>
                            </div>

                            <img src="images/icons/nodes.png" style={{transform: "rotate(90deg)"}} />

                            <div className="source-destination-chooser">
                                <p><b>Ziel:</b></p>
                                <select value={this.state.destination}
                                 onChange={event => this.setState({destination: event.target.value})}>
                                    <option>Kafka</option>
                                    <option>Hadoop</option>
                                </select>
                                <input required={true} value={this.state.topicOrDataOut}
                                 onChange={event => this.setState({topicOrDataOut: event.target.value})}/>
                            </div>
                        </div>

                        <div style={{display: "flex", padding: "5px"}}>
                            <span className="bottom-options" disabled={!this.state.edit}>
                                {!this.state.forgetting &&
                                    <span>
                                        <label>Minimale benötigte Datensatzmenge:</label>
                                        <input type="number" min={1} max={Number.MAX_VALUE} value={this.state.minDataSets}
                                         onChange={(event) => this.setState({minDataSets: event.target.value})}/>
                                    </span>
                                }
                                <label htmlFor="forgetting">〈 Vorige Datensätze verwenden
                                    <input name="forgetting" type="checkbox" value={!this.state.forgetting}
                                     onChange={() => this.setState({forgetting: !this.state.forgetting})}/> 〉
                                </label>
                                {!this.state.forgetting &&
                                    <span>
                                        <label>Maximal erlaubte Datensatzmenge:</label>
                                        <input type="number" min={this.state.minDataSets} max={Number.MAX_VALUE}
                                         value={this.state.maxDataSets}
                                         onChange={(event) => this.setState({maxDataSets: event.target.value})} />
                                    </span>
                                }
                            </span>
                        </div>
                    </div>
                }
            </div>
        );
    }
}