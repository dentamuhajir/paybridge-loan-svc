pipeline {
    agent any

    environment {
        IMAGE_NAME = "dentamuhajir/paybridge-loan-svc"
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {

        // ================================
        // STAGE 1: Checkout Source Code
        // ================================
        stage('Checkout') {
            steps {
                echo "======== Checking out source code ========"
                checkout scm
            }
        }

        // ================================
        // STAGE 2: Build Docker Image
        // ================================
        stage('Build Docker Image') {
            steps {
                echo "======== Building Docker Image ========"
                sh """
                    DOCKER_BUILDKIT=0 docker build \
                        --target prod \
                        -t ${IMAGE_NAME}:${IMAGE_TAG} \
                        -t ${IMAGE_NAME}:latest \
                        -f Dockerfile .
                """
            }
        }

        // ================================
        // STAGE 3: Push to Docker Hub
        // ================================
        stage('Push to Docker Hub') {
            steps {
                echo "======== Logging into Docker Hub ========"

                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {

                    sh """
                        echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
                    """

                    echo "======== Pushing Image to Docker Hub ========"

                    retry(3) {
                        sh "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
                        sh "docker push ${IMAGE_NAME}:latest"
                    }
                }

                echo "======== Image pushed: ${IMAGE_NAME}:${IMAGE_TAG} ========"
            }
        }
    }

    post {
        success {
            echo "CI Successful!"
            echo "Image available at:"
            echo "https://hub.docker.com/r/dentamuhajir/paybridge-loan-svc"
            echo "Tags pushed:"
            echo "${IMAGE_NAME}:${IMAGE_TAG}"
            echo "${IMAGE_NAME}:latest"
        }

        failure {
            echo "CI Failed! Check build logs above."
        }

        always {
            echo "Cleaning up local Docker images..."

            sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true"
            sh "docker rmi ${IMAGE_NAME}:latest || true"
            sh "docker logout || true"
        }
    }
}