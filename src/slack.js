const request = require('./request')

exports.notify = (hook, payload) => {
    return request.post(hook, payload).then(response => {
        console.info(`Slack API responded with ${response.statusCode}: ${response.body}`)
        return `Successfully notified about ${payload}`
    }).catch(e => {
        const message = `Slack API responded with ${e.statusCode}: ${e.body}`
        console.error(message)
        return Promise.reject(message)
    })
}
