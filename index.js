const slack = require('./slack')

exports.handler = async () => slack.notify(process.env.SLACK_HOOK)

