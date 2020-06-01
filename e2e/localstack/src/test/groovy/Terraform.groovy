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
        static generate(LocalStack localstack, String stateBucket) {
            new File('provider.tf').text = """
terraform {
  backend "s3" {
    bucket = "${stateBucket}"
    key = "terraform.tfstate"
    
    force_path_style = true
    
    endpoint = "${localstack.endpoint}"
    access_key = "${localstack.accessKey}"
    secret_key = "${localstack.secretKey}"
    region = "${localstack.region}"
    skip_credentials_validation = true
  }
}

provider "aws" {
  endpoints {
    iam = "${localstack.endpoint}"
    lambda = "${localstack.endpoint}"
    cloudwatchlogs = "${localstack.endpoint}"
  }
  
  access_key = "${localstack.accessKey}"
  secret_key = "${localstack.secretKey}"
  region = "${localstack.region}"
  skip_credentials_validation = true
  skip_requesting_account_id = true
   
  s3_force_path_style = true
}"""
        }
    }

    static class Module {
        static generate(Map variables = [:], String region) {
            new File('main.tf').text = """
module "ninshubur" {
  source = "../../infrastructure"
  source_path = "../../src"
  region = "$region"
  ${variables.collect { k, v -> "$k = ${serialize(v)}" }.join('\n  ')}
}"""
        }

        private static serialize(value) {
            value instanceof Map ? "{${value.collect { k, v -> "$k: \"$v\"" }.join('\n')}}" : "\"$value\""
        }
    }

}
