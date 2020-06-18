pipeline {

	agent any

	stages
		stage ('Compile stage') {
			steps {
                withMaven(maven : 'maven_maven_3_6_3') {
                    sh 'mvn clean compile'
                }
			}
		}

		stage ('Testing stage') {
			steps {
			    withMaven(maven : 'maven_maven_3_6_3') {
					sh 'mvn test'
				}
			}
		}
	}

}