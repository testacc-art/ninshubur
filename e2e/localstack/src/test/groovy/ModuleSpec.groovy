import com.github.tomakehurst.wiremock.WireMockServer
import org.testcontainers.containers.localstack.LocalStackContainer
import software.amazon.awssdk.services.iam.IamClient
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.s3.S3Client
import spock.lang.Ignore
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*

class ModuleSpec extends Specification {

    def localstack = new LocalStackContainer()
            .withServices(S3, IAM, LAMBDA)
    LambdaClient lambda
    IamClient iam
    def stateBucket = 'ninshubur'

    def mock = new WireMockServer(wireMockConfig().httpsPort(443))

    def setup() {
        localstack.start()

        S3Client s3 = AWS.s3(localstack)
        s3.createBucket { it.bucket(stateBucket) }

        Terraform.Provider.generate(localstack, stateBucket)

        lambda = AWS.lambda(localstack)
        iam = AWS.iam(localstack)
    }

    @Ignore('Cannot bind https port on Travis CI')
    def 'a user can invoke the lambda function'() {
        given:
        mock.start()
        mock.givenThat(post(urlEqualTo('/hook'))
                .willReturn(ok()))
        and:
        Terraform.Module.generate(slack_hook: 'https://host.docker.internal/hook')
        Terraform.init()

        when:
        def apply = Terraform.apply()

        then:
        apply.exitValue == 0

        when:
        def result = lambda.invoke { it.functionName('ninshubur') }

        then:
        result.statusCode() == 200
        mock.findAll(postRequestedFor(urlEqualTo('/hook')))

        cleanup:
        mock.stop()
    }

    def 'a user can tag AWS resources'() {
        given:
        Terraform.Module.generate(tags: [foo: 'bar'], slack_hook: 'https://hooks.slack.com/hook')
        Terraform.init()

        when:
        def apply = Terraform.apply()

        then:
        apply.exitValue == 0

        expect:
        /* TODO check why lambda tags are missing */
        //  lambda.getFunction({ it.functionName('ninshubur') }).tags() == [foo: 'bar']
        iam.listRoleTags({ it.roleName('ninshubur') }).tags().collectEntries { [(it.key()): it.value()] } == [foo: 'bar']
    }

    def 'Slack hook must be a valid URL'() {
        given:
        Terraform.Module.generate(slack_hook: '/hook')
        Terraform.init()

        when:
        def apply = Terraform.apply()

        then:
        apply.exitValue != 0
        apply.error.contains 'Slack hook must be a valid URL.'
    }

    @Ignore
    def 'lambda execution results are persisted in CloudWatch'() {
        Terraform.Module.generate(slack_hook: 'https://hooks.slack.com/hook')
        Terraform.init()

        when:
        def apply = Terraform.apply()

        then:
        apply.exitValue == 0

        when:
        def result = lambda.invoke { it.functionName('ninshubur') }

        then:
        result.statusCode() == 200
        // TODO validate CloudWatch logs are present
    }

    def cleanup() {
        localstack.stop()
    }

}