const messages = require('./messages')
const supported_levels = ['info', 'warning', 'error', 'fatal']

exports.message = (level) => {
    const l = level ? level : 'info'
    if (supported_levels.includes(l)) {
        return Promise.resolve(`${level === 'fatal' ? '<!here> ' : ''}${messages.get(l)}`)
    }
    return Promise.reject("Only 'info', 'warning', 'error' and 'fatal' log levels are supported")
}