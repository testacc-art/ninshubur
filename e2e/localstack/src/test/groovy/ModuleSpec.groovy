import org.testcontainers.containers.localstack.LocalStackContainer
import software.amazon.awssdk.services.s3.S3Client
import spock.lang.Specification

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*

class ModuleSpec extends Specification {

    def localstack = new LocalStackContainer()
            .withServices(S3, IAM, LAMBDA)
    S3Client s3
    def stateBucket = 'ninshubur'

    def setup() {
        localstack.start()
        Terraform.Provider.generate(localstack, stateBucket)
        s3 = AWS.S3.client(localstack.getEndpointOverride(S3))
    }

    def 'a user can create a Terraform module'() {
        given:
        s3.createBucket({ it.bucket(stateBucket) })

        when:
        def init = Process.run('terraform init --reconfigure')

        then:
        init.exitValue() == 0

        when:
        def apply = Process.run('terraform apply --auto-approve')

        then:
        apply.exitValue() == 0
    }

    def cleanup() {
        localstack.stop()
    }

}