import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

import static java.lang.System.err
import static org.testcontainers.containers.Network.newNetwork

class LocalStack {
    final GenericContainer container
    final accessKey = 'foo'
    final secretKey = 'bar'
    final region = 'eu-west-1'
    final network = newNetwork()

    LocalStack() {
        // TODO switch to a released version once https://github.com/localstack/localstack/pull/2829/files is released
        container = new GenericContainer('localstack/localstack:latest')
                .withEnv(
                        SERVICES: 'kms,s3,lambda,iam',
                        DEFAULT_REGION: region,
                        LAMBDA_EXECUTOR: 'docker',
                        LAMBDA_DOCKER_NETWORK: network.id)
                .withNetwork(network)
                .withNetworkAliases('localstack')
                .withExposedPorts(4566)
                .waitingFor(Wait.forLogMessage(/.*Ready[.].*/, 1))
        container.withFileSystemBind('//var/run/docker.sock', '/var/run/docker.sock')
    }

    def start() {
        try {
            container.start()
        } catch (Exception e) {
            err.println 'LocalStack failed to start'
            err.println container.logs
            throw e
        }
    }

    def stop() {
        container.stop()
        network.close()
    }

    URI getEndpoint() {
        "http://localhost:${container.getMappedPort(4566)}".toURI()
    }
}