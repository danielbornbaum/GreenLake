import React from "react"
 import {InstallationPage} from "../InstallationPage.js"

 export class SetupPage1 extends React.Component{
     render(){
         return(
             <div style={{maxHeight:"100%"}}>
                 <InstallationPage nextText="weiter" installationGoal="Kafka" id="setupPage1"
                  installationURL="/greenlake-platform/setup/installKafka"
                  verificationURL="/greenlake-platform/setup/validateKafkaAtLocation"
                  progressURL="/greenlake-platform/setup/kafkaSetupProgress"
                  licenseNotice="Installing Apache Kafka is the equivalent of downloading and untaring Kafka from the official website, hence you agree to the Apache License 2.0."
                  nextAction={this.props.nextAction}
                  previousAction={this.props.previousAction}/>
             </div>
         );
     }
 }