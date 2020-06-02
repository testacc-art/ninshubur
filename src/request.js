const https = require('https')

exports.perform = async (options, payload) => new Promise((resolve, reject) => {
    const body = JSON.stringify(payload)
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