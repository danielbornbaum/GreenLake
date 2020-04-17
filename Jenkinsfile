node {
	stage('Checkout') {
		git credentialsId: 'GitHubSSH',
		    url: 'git@github.com:danielbornbaum/GreenLake.git'
	}
	
	stage('Build') {
		sh "/usr/share/maven/bin/mvn -B package -f greenlake-platform"
	}
	
	stage('Deploy .ear (wildfly)') {
		sh 'sudo cp /var/lib/jenkins/workspace/GreenLake/greenlake-platform/greenlake-ear/target/greenlake-platform.ear /opt/wildfly-19.0.0.Final/standalone/deployments/'
	}
	
	stage('Start wildfly server') {
		sh 'sudo systemctl start wildfly'	
	}
}
