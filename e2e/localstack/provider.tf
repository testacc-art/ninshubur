
terraform {
  backend "s3" {
    bucket = "ninshubur"
    key    = "terraform.tfstate"
    region = "us-east-1"
    endpoint = "http://127.0.0.1:36949"
    skip_credentials_validation = true
    force_path_style = true
  }
}

provider "aws" {
  region = "us-east-1"
  s3_force_path_style         = true
  skip_credentials_validation = true 
  endpoints {
    iam = "http://127.0.0.1:36928"
    lambda = "http://127.0.0.1:36947"
  }
}