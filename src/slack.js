const https = require('https')
const hook = require('./hook')

const options = () => hook.get().then(h => {
    return {
        method: 'POST',
        host: h.hostname,
        path: h.pathname,
        headers: {'Content-Type': 'application/json'}
    }
})

exports.notify = async () => options().then(call)

const call = async options => new Promise((resolve, reject) => {
    const body = JSON.stringify({
        text: 'test'
    })
    const request = https.request(
        options,
        response => {
            console.log(`Response code: ${response.statusCode}`)
            response.on('data', chunk => console.log(`Response body: ${chunk}`))
            if (response.statusCode >= 200 && response.statusCode < 300) {
                resolve(`Successfully notified about ${body}`)
            } else {
                reject(`Slack API responded with ${response.statusCode} code`)
            }
        }).on('error', console.error)
    request.write(body)
    request.end()
})
