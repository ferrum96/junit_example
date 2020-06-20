pipeline {
    agent any

    stages {

        stage ('Compile stage') {

        	steps {
                bat 'mvn clean compile'
            }
        }

        stage ('Testing stage') {

        	steps {
        		bat 'mvn test'
            }
        }
        stage ('Delete dir') {
            deleteDir()
            cleanWs()
        }

    }
}