import org.testcontainers.containers.localstack.LocalStackContainer

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*

class Terraform {

    static init() {
        def init = Process.run('terraform init --reconfigure')
        assert init.exitValue == 0
    }

    static apply() {
        Process.run('terraform apply --auto-approve')
    }

    static destroy() {
        Process.run('terraform destroy --auto-approve')
    }

    static class Provider {
        static generate(LocalStackContainer localstack, String stateBucket) {
            new File('provider.tf').text = """
terraform {
  backend "s3" {
    bucket = "${stateBucket}"
    key = "terraform.tfstate"
    
    force_path_style = true
    
    endpoint = "${localstack.getEndpointOverride(S3)}"
    access_key = "${localstack.accessKey}"
    secret_key = "${localstack.secretKey}"
    region = "${localstack.region}"
    skip_credentials_validation = true
  }
}

provider "aws" {
  endpoints {
    iam = "${localstack.getEndpointOverride(IAM)}"
    lambda = "${localstack.getEndpointOverride(LAMBDA)}"
  }
  
  access_key = "${localstack.accessKey}"
  secret_key = "${localstack.secretKey}"
  region = "${localstack.region}"
  skip_credentials_validation = true
   
  s3_force_path_style = true
}"""
        }
    }

    static class Module {
        static generate(Map variables = [:]) {
            new File('main.tf').text = """
module "ninshubur" {
  source = "../../infrastructure"
  source_path = "../../src"
  ${variables.collect { k, v -> "$k = ${serialize(v)}" }.join('\n  ')}
}"""
        }

        private static serialize(value) {
            value instanceof Map ? "{${value.collect { k, v -> "$k: \"$v\"" }.join('\n')}}" : "\"$value\""
        }
    }

}
