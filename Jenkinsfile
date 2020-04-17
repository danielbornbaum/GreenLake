node {
	stage('Checkout') {
		git credentialsId: 'GitHubSSH',
		    url: 'git@github.com:danielbornbaum/GreenLake.git'
	}
	
	stage('Build') {
		sh "$MAVEN_HOME/bin/mvn-B package"
	}
}
