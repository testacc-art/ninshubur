const https = require('https')

const options = hook => {
    return {
        method: 'POST',
        port: 443,
        host: 'hooks.slack.com',
        path: hook,
        headers: {'Content-Type': 'application/json'}
    }
}

exports.notify = async hook => {
    const body = JSON.stringify({
        text: 'test'
    })
    return new Promise((resolve, reject) => {
        const request = https.request(
            options(hook),
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
}