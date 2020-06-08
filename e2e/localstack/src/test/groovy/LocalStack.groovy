

import org.testcontainers.containers.localstack.LocalStackContainer

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.IAM
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.LAMBDA
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3

class LocalStack {
    final LocalStackContainer container

    LocalStack() {
        container = new LocalStackContainer('0.11.2')
                .withServices(S3, IAM, LAMBDA)
                .withExposedPorts(4566)
    }

    def start() {
        container.start()
    }

    def stop() {
        container.stop()
    }

    def getAccessKey() {
        container.accessKey
    }

    def getSecretKey() {
        container.secretKey
    }

    def getRegion() {
        container.region
    }

    URI getEndpoint() {
        "http://localhost:${container.getMappedPort(4566)}".toURI()
    }
}
