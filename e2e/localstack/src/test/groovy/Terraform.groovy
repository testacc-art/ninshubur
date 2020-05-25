import org.testcontainers.containers.localstack.LocalStackContainer

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.IAM
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.LAMBDA
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3

class Terraform {
    static class Provider {
        static generate(LocalStackContainer localstack, String stateBucket) {
            new File('provider.tf').text = """
terraform {
  backend "s3" {
    bucket = "${stateBucket}"
    key    = "terraform.tfstate"
    region = "${localstack.region}"
    endpoint = "${localstack.getEndpointOverride(S3)}"
    skip_credentials_validation = true
    force_path_style = true
  }
}

provider "aws" {
  region = "${localstack.region}"
  s3_force_path_style         = true
  skip_credentials_validation = true 
  endpoints {
    iam = "${localstack.getEndpointOverride(IAM)}"
    lambda = "${localstack.getEndpointOverride(LAMBDA)}"
  }
}"""
        }
    }
}
