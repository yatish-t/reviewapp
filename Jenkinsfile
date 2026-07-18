pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "yatisht1984/testreviewapp"
        DOCKER_TAG = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout Code') {
            steps {
                git branch: 'main', url: 'https://github.com/yatish-t/reviewapp.git'
            }
        }

        stage('Build Maven Artifact') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t $DOCKER_IMAGE:$DOCKER_TAG ."
            }
        }

        stage('Login to DockerHub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'Dockerhub',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                sh "docker push $DOCKER_IMAGE:$DOCKER_TAG"
            }
        }

        stage('Deploy To Eks') {
            steps {
                sh """
                kubectl apply -f deployment.yaml
                kubectl set image deployment/reviewapp reviewapp=yatisht1984/testreviewapp:${BUILD_NUMBER}
                """
            }
        }
    }
}
