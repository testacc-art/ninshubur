const hook = require('./hook')
const request = require('./request')

const options = () => hook.get().then(h => {
    return {
        method: 'POST',
        port: h.port,
        host: h.hostname,
        path: h.pathname,
        headers: {'Content-Type': 'application/json'}
    }
})

exports.notify = async () => options().then(o => request.perform(o, {text: 'test'}))
