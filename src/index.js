const slack = require('./slack')

exports.handler = async () => slack.notify()