const environment = require('./environment')
const slack = require('./slack')
const payload = require('./payload')

const validate = (event) => event ? Promise.resolve(event) : Promise.reject('Event is required')

exports.handler = async (event) => Promise
    .all([environment.hook(), validate(event).then(payload.generate)])
    .then(([h, p]) => slack.notify(h, p))