import org.testcontainers.containers.localstack.LocalStackContainer
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.awscore.client.builder.AwsSyncClientBuilder
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.iam.IamClient
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.s3.S3Client

import java.util.function.Supplier


class AWS {
    static s3(LocalStackContainer localstack) {
        client(
                { S3Client.builder() },
                localstack,
                LocalStackContainer.Service.S3)
    }

    static lambda(LocalStackContainer localstack) {
        client(
                { LambdaClient.builder() },
                localstack,
                LocalStackContainer.Service.LAMBDA)
    }

    static iam(LocalStackContainer localstack) {
        client(
                { IamClient.builder() },
                localstack,
                LocalStackContainer.Service.IAM)
    }

    private static <A extends AwsSyncClientBuilder<A, B>, B> B client(
            Supplier<AwsSyncClientBuilder<A, B>> supplier,
            LocalStackContainer localstack,
            LocalStackContainer.Service service) {
        supplier.get()
                .credentialsProvider(credentials(localstack))
                .region(Region.of(localstack.region))
                .endpointOverride(localstack.getEndpointOverride(service))
                .build()
    }

    private static credentials(LocalStackContainer localstack) {
        def credentials = AwsBasicCredentials.create(localstack.accessKey, localstack.secretKey)
        StaticCredentialsProvider.create(credentials)
    }
}
