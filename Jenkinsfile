pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9.5' // Adjust to your Jenkins Maven installation name
        jdk 'JDK-11' // Adjust to your Jenkins JDK installation name
    }
    
    environment {
        MAVEN_OPTS = '-Xmx2048m'
        GAME_PATH = "${env.WORKSPACE}/../CudaGame"
        TEST_RESULTS = 'target/surefire-reports'
        INTEGRATION_TEST_RESULTS = 'target/failsafe-reports'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                    env.GIT_BRANCH = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                }
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Unit Tests - JUnit') {
            steps {
                sh 'mvn test -Pjunit-only'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: "${TEST_RESULTS}/*.xml"
                }
            }
        }
        
        stage('Unit Tests - TestNG') {
            steps {
                sh 'mvn test -Ptestng-only'
            }
            post {
                always {
                    publishHTML(target: [
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/surefire-reports',
                        reportFiles: 'index.html',
                        reportName: 'TestNG Report'
                    ])
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
                sh 'mvn verify -DskipUnitTests'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: "${INTEGRATION_TEST_RESULTS}/*.xml"
                }
            }
        }
        
        stage('Code Coverage') {
            steps {
                sh 'mvn jacoco:report'
                publishHTML(target: [
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/site/jacoco',
                    reportFiles: 'index.html',
                    reportName: 'JaCoCo Coverage Report'
                ])
            }
        }
        
        stage('Generate Allure Report') {
            steps {
                script {
                    allure([
                        includeProperties: false,
                        jdk: '',
                        properties: [],
                        reportBuildPolicy: 'ALWAYS',
                        results: [[path: 'target/allure-results']]
                    ])
                }
            }
        }
        
        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                archiveArtifacts artifacts: 'target/surefire-reports/**/*', allowEmptyArchive: true
                archiveArtifacts artifacts: 'target/failsafe-reports/**/*', allowEmptyArchive: true
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo "Pipeline completed successfully!"
            // Can add Slack/email notifications here
        }
        failure {
            echo "Pipeline failed!"
            // Can add Slack/email notifications here
        }
    }
}
