const level = require('./level')
const environment = require('./environment')

const fields = details => {
    if (details) {
        if (Object.keys(details).length > 0) {
            return Promise.resolve(Object.entries(details).map(e => {
                return {
                    title: e[0],
                    value: e[1],
                    short: true
                }
            }))
        }
        return Promise.reject('At least one detail is required')
    }
    return Promise.reject('Details are required')
}

const payload = (event) => Promise.all([level.message(event.level), level.color(event.level), fields(event.details)])
    .then(([message, color, fields]) => {
        return {
            username: environment.name(),
            text: message,
            icon_url: environment.avatar_url(),
            attachments: [{
                color: color,
                fields: fields
            }]
        }
    })

exports.generate = payload