# 𒀭𒊩𒌆𒋚 (Ninshubur) [![Build Status](https://travis-ci.org/artamonovkirill/ninshubur.svg?branch=master)](https://travis-ci.org/artamonovkirill/ninshubur)

> Delivers your messages. With a twist.

[Ninshubur](https://en.wikipedia.org/wiki/Ninshubur) is a [AWS Lambda](https://aws.amazon.com/lambda/) 
function that allows you to post [Slack](https://slack.com/) notifications

## NB!

This is still work in progress.

TODO:
* random messages
* fields support
* log levels support
* (customizable) avatar
* (customizable) name
* fetch lambda from S3

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

### Install dependencies
```bash
npm install
```

### Test
```bash
npm test
```

### E2E test

E2E test is performed with [node-lambda](https://github.com/motdotla/node-lambda).

* Create `e2e/deploy.env`
* Put `SLACK_HOOK=...` in `e2e/deploy.env` 
* Run `npm run e2e`