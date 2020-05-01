import React from "react"
import {InstallationPage} from "../InstallationPage.js"

export class SetupPage3 extends React.Component{
    render(){
        return(
            <div style={{maxHeight:"100%"}}>
                <InstallationPage nextText="fertig stellen" installationGoal="Hadoop" id="setupPage3"
                 installationURL="/greenlake-platform/setup/installHadoop"
                 verificationURL="/greenlake-platform/setup/validateHadoopAtLocation"
                 progressURL="/greenlake-platform/setup/hadoopSetupProgress"
                 licenseNotice="Installing Apache Hadoop is the equivalent of downloading and untaring Hadoop from the official website, hence you agree to the Apache License 2.0."
                 nextAction={this.props.nextAction}
                 previousAction={this.props.previousAction}/>
            </div>
        );
    }
}