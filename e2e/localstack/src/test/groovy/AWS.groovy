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
    static s3(LocalStack localstack) {
        client(
                { S3Client.builder() },
                localstack)
    }

    static lambda(LocalStack localstack) {
        client(
                { LambdaClient.builder() },
                localstack)
    }

    static iam(LocalStack localstack) {
        client(
                { IamClient.builder() },
                localstack)
    }

    private static <A extends AwsSyncClientBuilder<A, B>, B> B client(
            Supplier<AwsSyncClientBuilder<A, B>> supplier,
            LocalStack localstack) {
        supplier.get()
                .credentialsProvider(credentials(localstack))
                .region(Region.of(localstack.region))
                .endpointOverride(localstack.endpoint)
                .build()
    }

    private static credentials(LocalStack localstack) {
        def credentials = AwsBasicCredentials.create(localstack.accessKey, localstack.secretKey)
        StaticCredentialsProvider.create(credentials)
    }
}
