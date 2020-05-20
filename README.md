# 𒀭𒊩𒌆𒋚 (Ninshubur)

> Delivers your messages. With a twist.

[Ninshubur](https://en.wikipedia.org/wiki/Ninshubur) is a [AWS Lambda](https://aws.amazon.com/lambda/) 
function that allows you to post [Slack](https://slack.com/) notifications

## Features

* Severity levels expressed by colors:
  * Info: green;
  * Warning: yellow;
  * Error: red;
  * Fatal: red with `@here` mention.
* Integration with [AWS KMS](https://aws.amazon.com/kms/).
* Random quotes from movies and games to add a bit of fun.
* A name and avatar are configurable.
* Deployable with terraform.

## Building a project

* Install dependencies: `npm install`