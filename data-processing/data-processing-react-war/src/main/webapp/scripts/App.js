import React from "react"
import ReactDOM from "react-dom"
import {JobConfigurator} from "./components/JobConfigurator"
import {restRequest} from "./util/RestManager"

class App extends React.Component{

    defaultJob = {source: "Kafka", destination: "Kafka", javascript: "var results = data;", minDataSetSize: 1,
                  maxDataSetSize: 1, topicOrDataIn: "", topicOrDataOut: "", forgetting: true, timeout: 1000,
                  schedulingTime: 1000, consumerGroup: "consumer-group", title: "Datenjob"}

    constructor(props){
        super(props);
        this.state = {jobs: []};
    }

    componentDidMount(){
        this.triggerReload();
    }

    triggerReload = () =>{
        restRequest("/data-processing-server/jobs/list", "GET", "", this.onReloadSuccess);
    }

    onReloadSuccess = (responseText) => {
        var jobs = JSON.parse(responseText).jobs;
        this.setState({jobs: jobs});
    }

    addJob = () => {
        restRequest("/data-processing-server/jobs/add", "POST", JSON.stringify(this.defaultJob),
        responseText => this.triggerReload, errorText => this.triggerReload);
    }

    render(){
        return(
            <div style={{overflow: "auto", paddingBottom: "70px"}}>
                {this.state.jobs.map(job =>
                    <JobConfigurator key={job.id} title={job.title} code={job.javascript} id={job.id}
                    forgetting={job.forgetting} schedulingTime={job.schedulingTime} timeout={job.timeout}
                    minDataSetSize={job.minDataSetSize} maxDataSetSize={job.maxDataSetSize}
                    consumerGroup={job.consumerGroup}/>
                )}

                <button className="job-add-button" onClick={this.addJob}>+</button>
                <button className="job-reload-button" onClick={this.triggerReload}>⟳</button>
            </div>
        )
    }
}

ReactDOM.render(<App />, document.getElementById("app"));