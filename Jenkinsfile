node {
	def mvnHome = tool 'M3'
	stage('Checkout') {
        	git 'https://github.com/danielbornbaum/GreenLake.git'
    	}
	
	stage('Build') {
		sh "${mvnHomw}/bin/maven-B package"
	}
}
