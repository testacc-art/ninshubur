import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.output.OutputFrame
import software.amazon.awssdk.services.s3.S3Client
import spock.lang.Specification
import terraform.Provider

import java.util.function.Consumer

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*
import static org.testcontainers.containers.output.OutputFrame.OutputType.STDERR

class TerraformModuleSpec extends Specification {

    def localstack = new LocalStackContainer()
            .withServices(S3, IAM, LAMBDA)
    S3Client s3
    def stateBucket = 'ninshubur'

    def setup() {
        localstack.start()
        Provider.generate(localstack, stateBucket)
        s3 = S3Client
                .builder()
                .endpointOverride(localstack.getEndpointOverride(S3))
                .build()
    }

    def 'a user can create a Terraform module'() {
        given:
        s3.createBucket({ it.bucket(stateBucket) })

        when:
        def init = run('terraform init --reconfigure')

        then:
        init.exitValue() == 0

        when:
        def apply = run('terraform apply --auto-approve')

        then:
        apply.exitValue() == 0
    }

    def cleanup() {
        println localstack.getLogs(STDERR)
        localstack.stop()
    }

    static run(String command) {
        def process = Runtime.runtime.exec(command)

        join(process.inputStream, { System.out.println(it) })
        join(process.errorStream, { System.err.println(it) })

        process.waitFor()
        process
    }

    static join(InputStream stream, Consumer<String> logger) {
        def bufferedReader = new BufferedReader(new InputStreamReader(stream))
        String line
        while ((line = bufferedReader.readLine()) != null) {
            logger(line)
        }
    }

    static delete(String path) {
        def file = new File(path)
        file.isDirectory() ? file.deleteDir() : file.delete()
        assert !file.exists()
    }

}