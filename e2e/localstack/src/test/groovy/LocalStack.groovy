import org.testcontainers.containers.localstack.LocalStackContainer

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*

class LocalStack {
    final container = new LocalStackContainer('0.11.2')
            .withServices(S3, IAM, LAMBDA)
            .withExposedPorts(4566)

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
