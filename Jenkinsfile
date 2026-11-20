// ============================================================
// Jenkinsfile - CI pipeline for the Student Management System
//
// Flow: GitHub -> Checkout -> Build -> Test -> Package -> Docker Build
// Each stage runs only if the previous one succeeded, so the Docker
// image is built only after all Maven tests pass.
//
// Before the first run, in Jenkins go to
//   Manage Jenkins -> Tools
// and add a JDK named "JDK17" and a Maven installation named "Maven3".
// ============================================================

pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    environment {
        IMAGE_NAME = 'student-management-system'
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
    }

    options {
        // Keep only the last 10 builds
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Getting the source code from GitHub...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Compiling the Java code with Maven...'
                runCmd('mvn -B clean compile')
            }
        }

        stage('Test') {
            steps {
                echo 'Running unit and integration tests...'
                runCmd('mvn -B test')
            }
            post {
                always {
                    // Shows test results (passed/failed) on the Jenkins build page
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                echo 'Creating the executable JAR...'
                // Tests already ran in the Test stage, so skip them here
                runCmd('mvn -B package -DskipTests')
            }
            post {
                success {
                    // Saves the JAR so it can be downloaded from the Jenkins build page
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building the Docker image from the JAR...'
                runCmd("docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest .")
                runCmd('docker images student-management-system')
            }
        }
    }

    post {
        success {
            echo "SUCCESS: image ${IMAGE_NAME}:${IMAGE_TAG} is ready. Run it with: docker compose up -d"
        }
        failure {
            echo 'FAILED: open the stage that turned red and read its console output.'
        }
    }
}

// Runs a command with "sh" on Linux/macOS Jenkins and with "bat" on Windows Jenkins
def runCmd(String command) {
    if (isUnix()) {
        sh command
    } else {
        bat command
    }
}
