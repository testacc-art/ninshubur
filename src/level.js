const messages = require('./messages')
const supported_levels = ['info', 'warning', 'error', 'fatal']

exports.parse = (level) => {
    const l = level ? level : 'info'
    if (supported_levels.includes(l)) {
        return Promise.resolve({
            text: `${level === 'fatal' ? '<!here> ' : ''}${messages.get(l)}`,
            color: '#11c560'
        })
    } else
        return Promise.reject("Only 'info', 'warning', 'error' and 'fatal' log levels are supported")
}