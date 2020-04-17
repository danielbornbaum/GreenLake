node {
	stage('Checkout') {
        	checkout(
			url: 'git@github.com:danielbornbaum/GreenLake.git',
			credentialsId: 'GitHubSSH',
			refspec: '+refs/heads/master:refs/remote/master'
		)
	}
	
	stage('Build') {
		sh "$MAVEN_HOME/bin/mvn-B package"
	}
}
