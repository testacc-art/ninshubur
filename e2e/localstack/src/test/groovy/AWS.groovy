import org.testcontainers.containers.localstack.LocalStackContainer
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client


class AWS {
    static class S3 {
        static client(LocalStackContainer localstack) {
            S3Client
                    .builder()
                    .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.S3))
                    .region(Region.of(localstack.region))
                    .build()
        }
    }
}
