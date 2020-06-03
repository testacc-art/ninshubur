const request = require('./request')

const options = hook => {
    return {
        method: 'POST',
        port: hook.port,
        host: hook.hostname,
        path: hook.pathname,
        headers: {'Content-Type': 'application/json'}
    }
}

exports.notify = async (hook, payload) => request.perform(options(hook), payload)
