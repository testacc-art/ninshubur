const https = require("https")
const http = require("http")

exports.post = (url, payload) => {
    const options = {
        method: 'POST',
        headers: {'Content-Type': 'application/json'}
    }
    const json = JSON.stringify(payload)
    const module = url.protocol === "https:" ? https : http

    return new Promise((resolve, reject) => {
        const request = module.request(url, options, response => {
            const bytes = []
            response.on('data', (chunk) => bytes.push(chunk))
            response.on('end', () => {
                const body = Buffer.concat(bytes).toString()
                response = {
                    statusCode:response.statusCode,
                    body: body
                }
                return response.statusCode === 200 ? resolve(response) : reject(response)
            })
        })
        request.on('error', reject)
        request.on('timeout', () => {
            request.destroy()
            reject('Request timeed out')
        })
        request.write(json)
        request.end()
    })
}