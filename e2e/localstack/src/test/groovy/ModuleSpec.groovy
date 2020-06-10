import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient
import software.amazon.awssdk.services.cloudwatchlogs.model.OutputLogEvent
import software.amazon.awssdk.services.iam.IamClient
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.LambdaException
import software.amazon.awssdk.services.s3.S3Client
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class ModuleSpec extends Specification {

    def stateBucket = 'ninshubur'
    def localstack = new LocalStack()

    LambdaClient lambda
    IamClient iam
    CloudWatchLogsClient cloudWatchLogs

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
        Terraform.Module.generate(slack_hook: 'https://httpbin.org/post', region: 'eu-west-1')
        Terraform.init()
        and:
        def payload = SdkBytes.fromUtf8String('{"details": {"project": "Ninshubur"}}')

        when:
        def apply = Terraform.apply()

        then:
        apply.exitValue == 0

        when:
        def result = lambda.invoke { it.functionName('ninshubur').payload(payload) }

        then:
        result.statusCode() == 200
        !result.functionError()
        result.payload().asUtf8String() =~ 'Successfully notified'
        and:
        new PollingConditions(timeout: 10).eventually {
            cloudWatchLogs('/aws/lambda/ninshubur').find { it.message() =~ 'Slack API responded with 200' }
        }
    }

    def 'an unsuccessful notification fails the lambda invocation'() {
        given:
        Terraform.Module.generate(slack_hook: 'https://httpbin.org/status/400', region: 'eu-west-1')
        Terraform.init()
        and:
        def payload = SdkBytes.fromUtf8String('{"details": {"project": "Ninshubur"}}')

        when:
        def apply = Terraform.apply()

        then:
        apply.exitValue == 0

        when:
        lambda.invoke { it.functionName('ninshubur').payload(payload) }

        then:
        def e = thrown LambdaException
        e.statusCode() == 500
        e.awsErrorDetails().errorMessage() =~ 'Slack API responded with 400'
    }

    List<OutputLogEvent> cloudWatchLogs(String groupName) {
        cloudWatchLogs.describeLogStreams { it.logGroupName(groupName) }.logStreams()
                .collectMany { s -> cloudWatchLogs.getLogEvents { it.logGroupName('/aws/lambda/ninshubur').logStreamName(s.logStreamName()) }.events() }
    }

    def 'a user can tag AWS resources'() {
        given:
        def tags = [foo: 'bar']
        Terraform.Module.generate(tags: tags, slack_hook: 'https://hooks.slack.com/hook', region: 'eu-west-1')
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
        Terraform.Module.generate(slack_hook: '/hook', region: 'eu-west-1')
        Terraform.init()

        when:
        def apply = Terraform.apply()

        then:
        apply.exitValue != 0
        apply.error.contains 'Slack hook must be a valid URL.'
    }

    def 'a user can configure Slack notification username'() {
        given:
        Terraform.Module.generate(slack_hook: 'https://httpbin.org/post', name: 'Geronimo', region: 'eu-west-1')
        Terraform.init()
        and:
        def payload = SdkBytes.fromUtf8String('{"details": {"project": "Ninshubur"}}')

        when:
        def apply = Terraform.apply()

        then:
        apply.exitValue == 0

        when:
        def result = lambda.invoke { it.functionName('ninshubur').payload(payload) }

        then:
        result.statusCode() == 200
        !result.functionError()
        result.payload().asUtf8String() =~ 'Successfully notified'
        and:
        new PollingConditions(timeout: 10).eventually {
            cloudWatchLogs('/aws/lambda/ninshubur').find { it.message() =~ '"username": "Geronimo"' }
        }
    }

    def 'a user can configure Slack notification avatar'() {
        given:
        Terraform.Module.generate(
                slack_hook: 'https://httpbin.org/post',
                avatar_url: 'https://google.be',
                region: 'eu-west-1')
        Terraform.init()
        and:
        def payload = SdkBytes.fromUtf8String('{"details": {"project": "Ninshubur"}}')

        when:
        def apply = Terraform.apply()

        then:
        apply.exitValue == 0

        when:
        def result = lambda.invoke { it.functionName('ninshubur').payload(payload) }

        then:
        result.statusCode() == 200
        !result.functionError()
        result.payload().asUtf8String() =~ 'Successfully notified'
        and:
        new PollingConditions(timeout: 10).eventually {
            cloudWatchLogs('/aws/lambda/ninshubur').find { it.message() =~ '"icon_url": "https://google.be"' }
        }
    }

    def 'Avatar URL must be a valid URL'() {
        given:
        Terraform.Module.generate(slack_hook: 'https://httpbin.org/post', avatar_url: 'foo', region: 'eu-west-1')
        Terraform.init()

        when:
        def apply = Terraform.apply()

        then:
        apply.exitValue != 0
        apply.error.contains 'Avatar URL must be a valid URL.'
    }

    def 'Only eu-west-1 region is supported at the moment'() {
        given:
        Terraform.Module.generate(slack_hook: 'https://httpbin.org/post', region: 'us-east-1')
        Terraform.init()

        when:
        def apply = Terraform.apply()

        then:
        apply.exitValue != 0
        apply.error.contains 'Only eu-west-1 region is supported at the moment.'
    }

    def cleanup() {
        localstack.stop()
    }

}