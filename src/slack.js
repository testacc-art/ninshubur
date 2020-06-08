const fetch = require('node-fetch')

const options = body => {
    return {
        body: body,
        method: 'POST',
        headers: {'Content-Type': 'application/json'}
    }
}

const checkIsSuccessful = (response, body) => response.text().then(b => {
    const message = `Slack API responded with ${response.status}: ${b}`
    console.log(message)
    return response.ok
        ? Promise.resolve(`Successfully notified about ${body}`)
        : Promise.reject(message)
})

exports.notify = (hook, payload) => {
    const body = JSON.stringify(payload)
    return fetch(hook, options(body))
        .then(r => checkIsSuccessful(r, body))
}
