node {
	stage('Checkout') {
        	git 'https://github.com/danielbornbaum/GreenLake.git'
    	}
	
	stage('Build') {
		sh "$MAVEN_HOME/bin/mvn-B package"
	}
}
