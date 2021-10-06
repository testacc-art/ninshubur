import org.junit.rules.TemporaryFolder

class Terraform {

    static init() {
        def init = Process.run('terraform init --reconfigure')
        assert init.exitValue == 0
    }

    static apply() {
        Process.run('terraform apply --auto-approve')
    }

    static class Provider {
        static generate(LocalStack localstack, String stateBucket) {
            new File('provider.tf').text = """
terraform {
  required_providers {
    aws = "3.61.0"
    null = "3.1.0"
    archive = "2.2.0"
  }

  backend "s3" {
    bucket = "${stateBucket}"
    key = "terraform.tfstate"
    region = "eu-west-1"
    
    force_path_style = true
    
    endpoint = "${localstack.endpoint}"
    access_key = "${localstack.accessKey}"
    secret_key = "${localstack.secretKey}"
    skip_credentials_validation = true
  }
}

provider "aws" {
  endpoints {
    iam = "${localstack.endpoint}"
    lambda = "${localstack.endpoint}"
    cloudwatchlogs = "${localstack.endpoint}"
    s3 = "${localstack.endpoint}"
    kms = "${localstack.endpoint}"
  }
  
  region = "eu-west-1"
  
  access_key = "${localstack.accessKey}"
  secret_key = "${localstack.secretKey}"
  skip_credentials_validation = true
  skip_requesting_account_id = true
   
  s3_force_path_style = true
}

provider "null" {
}

provider "archive" {
}"""
        }
    }

    static class Module {
        static generate(Map variables = [:], TemporaryFolder tmp) {
            new File('main.tf').text = """
module "ninshubur" {
  source = "../../infrastructure"
  s3_object_versions = {
    ${variables.region}: aws_s3_bucket_object.lambda.version_id
  }
  ${variables.collect { k, v -> "$k = ${serialize(v)}" }.join('\n  ')}
}

data "archive_file" "zip" {
  type = "zip"
  source_dir = "../../src"
  output_path = "${tmp.root}/ninshubur.zip"
}

resource "aws_s3_bucket" "storage" {
  bucket = "ninshubur-${variables.region}"
  versioning {
    enabled = true
  }
  acl = "public-read"
}

resource "aws_s3_bucket_object" "lambda" {
  bucket = aws_s3_bucket.storage.bucket
  key    = "lambda.zip"
  source = data.archive_file.zip.output_path
}
"""
        }

        private static serialize(value) {
            value instanceof Map ? "{${value.collect { k, v -> "$k: \"$v\"" }.join('\n')}}" : "\"$value\""
        }
    }

}
