#!/usr/bin/env bash

set -e

zip=/tmp/terraform.zip

latest_version=$(curl -fs https://checkpoint-api.hashicorp.com/v1/check/terraform | jq -r .current_version)

command -v terraform && current_version=$(terraform --version)

if [[ $current_version == *$latest_version ]]
then
  echo "Terraform $latest_version is already installed"
else
  curl -fso $zip \
    "https://releases.hashicorp.com/terraform/$latest_version/terraform_${latest_version}_linux_amd64.zip"
  unzip -o $zip -d "$HOME/tools"
  chmod +x "$HOME/tools/terraform"
fi