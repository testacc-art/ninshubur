import org.testcontainers.containers.localstack.LocalStackContainer
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client


class AWS {
    static class S3 {
        static client(LocalStackContainer localstack) {
            S3Client
                    .builder()
                    .credentialsProvider(credentials(localstack))
                    .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.S3))
                    .region(Region.of(localstack.region))
                    .build()
        }

        private static credentials(LocalStackContainer localstack) {
            def credentials = AwsBasicCredentials.create(localstack.accessKey, localstack.secretKey)
            StaticCredentialsProvider.create(credentials)
        }
    }
}
