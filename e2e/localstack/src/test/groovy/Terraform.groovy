import org.testcontainers.containers.localstack.LocalStackContainer

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*

class Terraform {
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
}
