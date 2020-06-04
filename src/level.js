const messages = require('./messages')
const colors = require('./colors')

const supported_levels = ['info', 'warning', 'error', 'fatal']

const get = (level, fn) => {
    const l = level ? level : 'info'
    return supported_levels.includes(l)
        ? Promise.resolve(fn(l))
        : Promise.reject("Only 'info', 'warning', 'error' and 'fatal' log levels are supported")
}

exports.message = level => get(level, l => `${l === 'fatal' ? '<!here> ' : ''}${messages.get(l)}`)

exports.color = level => get(level, colors.get)