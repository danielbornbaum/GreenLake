pipeline {
	stages {
		stage('Build') {
			steps {
				sh 'mvn -B -SkipTests clean package'
			}
		}
	}
}
