const hook = require('./hook')
const level = require('./level')
const slack = require('./slack')

const validate = (event) => event ? Promise.resolve(event) : Promise.reject('Event is required')

const payload = (event) => level.parse(event.level)

exports.handler = async (event) => Promise
    .all([hook.get(), validate(event).then(payload)])
    .then(([h, p]) => slack.notify(h, p))