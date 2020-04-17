node {
	stage('Checkout') {
		git credentialsId: 'GitHubSSH',
		    url: 'git@github.com:danielbornbaum/GreenLake.git'
	}
	
	stage('Build') {
		sh "/usr/share/maven/bin/mvn -B package -f greenlake-platform"
	}
}
