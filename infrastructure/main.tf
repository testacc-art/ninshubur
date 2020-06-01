terraform {
  experiments = [
    variable_validation
  ]
}

locals {
  name = "ninshubur"
  filename = "${local.name}.zip"
  log_group = "/aws/lambda/${local.name}"
}

resource "aws_iam_role" "_" {
  name = local.name
  tags = var.tags

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow"
    }
  ]
}
EOF
}

resource "aws_cloudwatch_log_group" "_" {
  name = local.log_group
  tags = var.tags
}

resource "aws_iam_role_policy" "_" {
  role = aws_iam_role._.id
  name = "CloudWatch"
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": [
        "arn:aws:logs:${var.region}:*:log-group:${local.log_group}:*"
      ],
      "Effect": "Allow"
    }
  ]
}
EOF
}

resource "aws_lambda_function" "_" {
  function_name = local.name
  description = "Slack notifications with a twist"
  handler = "index.handler"

  filename = local.filename

  role = aws_iam_role._.arn

  runtime = "nodejs12.x"

  environment {
    variables = {
      SLACK_HOOK = var.slack_hook
    }
  }

  tags = var.tags

  depends_on = [
    data.archive_file.zip
  ]
}

data "archive_file" "zip" {
  type = "zip"
  source_dir = var.source_path
  output_path = local.filename
}