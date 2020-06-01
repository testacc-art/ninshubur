import com.github.tomakehurst.wiremock.WireMockServer
import software.amazon.awssdk.services.iam.IamClient
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.s3.S3Client
import spock.lang.Ignore
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

class ModuleSpec extends Specification {

    def localstack = new LocalStack()
    LambdaClient lambda
    IamClient iam
    CloudWatchLogsClient cloudWatchLogs
    def stateBucket = 'ninshubur'

    def mock = new WireMockServer(wireMockConfig().httpsPort(8443))

    def setup() {
        localstack.start()

        S3Client s3 = AWS.s3(localstack)
        s3.createBucket { it.bucket(stateBucket) }

        Terraform.Provider.generate(localstack, stateBucket)

        lambda = AWS.lambda(localstack)
        iam = AWS.iam(localstack)
        cloudWatchLogs = AWS.cloudWatchLogs(localstack)
    }

    def 'a user can invoke the lambda function'() {
        given:
        mock.start()
        mock.givenThat(post(urlEqualTo('/hook'))
                .willReturn(ok()))
        and:
        Terraform.Module.generate(slack_hook: 'https://host.docker.internal:8443/hook')
        Terraform.init()

        when:
        def apply = Terraform.apply()

        then:
        apply.exitValue == 0

        when:
        def result = lambda.invoke { it.functionName('ninshubur') }

        then:
        result.statusCode() == 200
        and:
        cloudWatchLogs('/aws/lambda/ninshubur')
        and:
        mock.findAll(postRequestedFor(urlEqualTo('/hook')))

        cleanup:
        mock.stop()
    }

    def cloudWatchLogs(String groupName) {
        cloudWatchLogs.describeLogStreams { it.logGroupName(groupName) }.logStreams()
                .collect { s -> cloudWatchLogs.getLogEvents { it.logGroupName('/aws/lambda/ninshubur').logStreamName(s.logStreamName()) }.events() }.flatten()
    }

    def 'a user can tag AWS resources'() {
        given:
        def tags = [foo: 'bar']
        Terraform.Module.generate(tags: tags, slack_hook: 'https://hooks.slack.com/hook', localstack.region)
        Terraform.init()

        when:
        def apply = Terraform.apply()

        then:
        apply.exitValue == 0

        expect:
        lambda.getFunction({ it.functionName('ninshubur') }).tags() == tags
        iam.listRoleTags { it.roleName('ninshubur') }.tags().collectEntries { [(it.key()): it.value()] } == tags
        cloudWatchLogs.listTagsLogGroup { it.logGroupName('/aws/lambda/ninshubur') }.tags() == tags
    }

    def 'Slack hook must be a valid URL'() {
        given:
        Terraform.Module.generate(slack_hook: '/hook', localstack.region)
        Terraform.init()

        when:
        def apply = Terraform.apply()

        then:
        apply.exitValue != 0
        apply.error.contains 'Slack hook must be a valid URL.'
    }

    def cleanup() {
        localstack.stop()
    }

}