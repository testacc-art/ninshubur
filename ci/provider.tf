provider aws {
  region = "eu-west-1"
}

terraform {
  backend "s3" {
    key    = "terraform.state"
    bucket = "ninshubur-state"
    region = "eu-west-1"
  }
}