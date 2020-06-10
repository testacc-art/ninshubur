resource "aws_s3_bucket" "_" {
  bucket = "ninshubur-${var.region}"
  region = var.region
  acl = "public-read"
  versioning {
    enabled = true
  }
}

variable "region" {
  type = string
}

output "bucket_arn" {
  value = aws_s3_bucket._.arn
}