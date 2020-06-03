const hook = require('./hook')
const slack = require('./slack')
const payload = require('./payload')

const validate = (event) => event ? Promise.resolve(event) : Promise.reject('Event is required')

exports.handler = async (event) => Promise
    .all([hook.get(), validate(event).then(payload.generate)])
    .then(([h, p]) => slack.notify(h, p))