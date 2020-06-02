resource "aws_iam_user" "_" {
  name = "ninshubur"
}

resource "aws_iam_user_policy" "_" {
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "s3:PutObject",
      "Resource": "${module.eu-west-1.bucket_arn}/lambda.zip"
    }
  ]
}
EOF
  user = aws_iam_user._.name
  name = "upload"
}