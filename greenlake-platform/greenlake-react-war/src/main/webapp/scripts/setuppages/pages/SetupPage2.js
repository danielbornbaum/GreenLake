import React from "react"
import {ConfigurationPage} from "../ConfigurationPage.js"

export class SetupPage2 extends React.Component{
    render(){
        return(
            <div style={{maxHeight: "100%"}}>
                <ConfigurationPage nextText="weiter" configurationGoal="Kafka" id="setupPage2" properties={[
                    {name: "server.properties", label: "Kafka", type: "group"},
                    {name: "broker.id", label: "Broker Id", type: "num", defaultValue: 0},
                    {name: "num.network.threads", label: "Anzahl Netzwerk Threads", type: "num", defaultValue: 3},
                    {name: "num.io.threads", label: "Anzahl IO Threads", type: "num", defaultValue: 8},
                    {name: "socket.send.buffer.bytes", label: "Sendepuffer (bytes)", type: "num", defaultValue: 102400},
                    {name: "socket.receive.buffer.bytes", label: "Empfanspuffer (bytes)", type: "num",
                     defaultValue: 102400},
                    {name: "socket.request.max.bytes", label: "max. Anfragegröße (bytes)", type: "num",
                     defaultValue: 104857600},
                    {name: "log.dirs", label: "Log-File-Speicherort", defaultValue: "/tmp/kafka-logs"},
                    {name: "num.partitions", label: "Anzahl Partitionen", type: "num", defaultValue: 0},
                    {name: "num.recovery.threads.per.data.dir", label: "Anzahl Recovery Threads", type: "num",
                     defaultValue: 1},
                    {name: "offsets.topic.replication.factor", label: "Offset Topic Replication", type: "num",
                     defaultValue: 1},
                    {name: "transaction.state.log.replication.factor", label: "Transaction State Log Replication: ",
                    type: "num", defaultValue: 1},
                    {name: "transaction.state.log.min.isr", label: "Minimum in Sync Replications",
                    type: "num", defaultValue: 1},
                    {name: "log.retention.hours", label: "Log-Speicherdauer (Stunden)", type: "num", defaultValue: 168},
                    {name: "log.segment.bytes", label: "Log-Segmentgröße (bytes)", type: "num", defaultValue: 1073741824},
                    {name: "log.retention.check.interval.ms", label: "Log-Speicherdauer-Überprüfung (ms)",
                     type: "num", defaultValue: 300000},
                    {name: "zookeeper.connect", label: "Zookeeper-Adresse (from server)", defaultValue: "localhost: 2181"},
                    {name: "zookeeper.connection.timeout.ms", label: "Zookeeper-Verbinungstimeout (ms)", type: "num",
                     defaultValue: 6000},
                    {name: "group.initial.rebalance.delay.ms", label: "Initiale Gruppenbalancierung (ms)", type: "num",
                     defaultValue: 0},

                    {name: "zookeeper.properties", label: "Zookeeper", type: "group"},
                    {name: "dataDir", label: "Datenverzeichnis", defaultValue: "/tmp/zookeeper"},
                    {name: "clientPort", label: "Client-Port (siehe Zookeeper-Adresse)", type: "num",
                     defaultValue: 2181},
                    {name: "maxClientCnxns", label: "Max. Client-Verbindungen (0 = ∞)", defaultValue: 0},
                    {name: "admin.enableServer", type: "boolean", label: "Aktiviere Admin-Server", defaultValue: false},
                    {name: "admin.serverPort", type: "num", label: "Admin-Server-Port: ", defaultValue: 8081}
                 ]} nextAction={this.props.nextAction} previousAction={this.props.previousAction}
                 propertiesUrl="/greenlake-platform/setup/setKafkaProperties" />
            </div>
        );
    }
}