node {
	stage('Checkout') {
        	git(
			url: 'git@github.com:danielbornbaum/GreenLake.git',
			credentialsId: 'GitHubSSH'			    	
		)
	}
	
	stage('Build') {
		sh "$MAVEN_HOME/bin/mvn-B package"
	}
}
