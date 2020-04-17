unode {
	stage('Checkout') {
        	checkout scm: [$class: 'GitSCM', userRemoteConfigs: [url: 'git@github.com:danielbornbaum/GreenLake.git', credentialsId: 'GitHubSSH',]]
	}
	
	stage('Build') {
		sh "$MAVEN_HOME/bin/mvn-B package"
	}
}
