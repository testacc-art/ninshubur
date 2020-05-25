import software.amazon.awssdk.services.s3.S3Client

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3

class AWS {
    static class S3 {
        static client(URI endpoint) {
            S3Client
                    .builder()
                    .endpointOverride(endpoint)
                    .build()
        }
    }
}
