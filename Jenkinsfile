node {
	stage('Checkout') {
        	git(
			url: 'git@github.com:danielbornbaum/GreenLake.git',
			credentialsId: 'Jenkins'			    	
		)
	}
	
	stage('Build') {
		sh "$MAVEN_HOME/bin/mvn-B package"
	}
}
