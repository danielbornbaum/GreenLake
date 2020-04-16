node {
	def rtMaven = Artifactory.newMavenBuild()
	
	stage('Clone sources') {
        	git url: 'https://github.com/danielbornbaum/GreenLake.git'
    	}
	
	stage('Maven build') {
		buildInfo = rtMaven.run pom: 'greenlake-platform/pom.xml', goals: 'clean install'
	}
	
	stage('Publish build info') {
        	server.publishBuildInfo buildInfo
    	}
}
