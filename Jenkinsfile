pipeline {
    agent any
    
    tools {
        maven 'M3' // This should match your Maven installation name in Jenkins
        jdk 'JDK-11' // This should match your JDK installation name in Jenkins
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
                    if (isUnix()) {
                        env.GIT_COMMIT = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                        env.GIT_BRANCH = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                    } else {
                        env.GIT_COMMIT = bat(script: '@git rev-parse HEAD', returnStdout: true).trim()
                        env.GIT_BRANCH = bat(script: '@git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                    }
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn clean compile'
                    } else {
                        bat 'mvn clean compile'
                    }
                }
            }
        }
        
        stage('Unit Tests - JUnit') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn test -Pjunit-only'
                    } else {
                        bat 'mvn test -Pjunit-only'
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: "${TEST_RESULTS}/*.xml"
                }
            }
        }
        
        stage('Unit Tests - TestNG') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn test -Ptestng-only'
                    } else {
                        bat 'mvn test -Ptestng-only'
                    }
                }
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
                script {
                    if (isUnix()) {
                        sh 'mvn verify -DskipUnitTests'
                    } else {
                        bat 'mvn verify -DskipUnitTests'
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: "${INTEGRATION_TEST_RESULTS}/*.xml"
                }
            }
        }
        
        stage('Code Coverage') {
            steps {
                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        if (isUnix()) {
                            sh 'mvn jacoco:report'
                        } else {
                            bat 'mvn jacoco:report'
                        }
                    }
                }
                publishHTML(target: [
                    allowMissing: true,
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
