const nock = require('nock')

const index = require('./index')
const messages = require('./messages')

beforeEach(() => {
    delete process.env.SLACK_HOOK
})

describe('Main', () => {
    it('performs a call to Slack API', async () => {
        process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
        nock('https://hooks.slack.com')
            .post('/hook',
                {
                    text: /.+/,
                    attachments: [{
                        color: /#.+/,
                        fields: [{
                            short: true,
                            title: 'Project',
                            value: 'Ninshubur'
                        }]
                    }]
                },
                {
                    reqheaders: {
                        'Content-Type': 'application/json'
                    }
                })
            .reply(200)
        const event = {details: {Project: 'Ninshubur'}}

        await expect(index.handler(event)).resolves.toMatch(/Successfully notified about .+/)
    })

    it('fails on 4xx responses', async () => {
        process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
        nock('https://hooks.slack.com')
            .post('/hook',
                _ => true,
                {
                    reqheaders: {
                        'Content-Type': 'application/json'
                    }
                })
            .reply(400, 'invalid_payload')
        const event = {details: {Project: 'Ninshubur'}}

        await expect(index.handler(event)).rejects.toBe('Slack API responded with 400 code')
    })
})

describe('Messages', () => {
    it('default message is taken from info pool', async () => {
        process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
        nock('https://hooks.slack.com')
            .post('/hook',
                body => messages.info().includes(body.text),
                {
                    reqheaders: {
                        'Content-Type': 'application/json'
                    }
                })
            .reply(200)
        const event = {details: {Project: 'Ninshubur'}}

        await expect(index.handler(event)).resolves.toBeTruthy()
    })

    it('info message is taken from info pool', async () => {
        process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
        nock('https://hooks.slack.com')
            .post('/hook',
                body => messages.info().includes(body.text),
                {
                    reqheaders: {
                        'Content-Type': 'application/json'
                    }
                })
            .reply(200)
        const event = {level: 'info', details: {Project: 'Ninshubur'}}

        await expect(index.handler(event)).resolves.toBeTruthy()
    })

    test.each(['error', 'warning', 'fatal'])('%s message is taken from error pool', async level => {
        process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
        nock('https://hooks.slack.com')
                .post('/hook',
                    body => messages.errors().includes(body.text.replace('<!here> ', '')),
                    {
                        reqheaders: {
                            'Content-Type': 'application/json'
                        }
                    })
                .reply(200)
        const event = {level: level, details: {Project: 'Ninshubur'}}

        await expect(index.handler(event)).resolves.toBeTruthy()
    })

    it('fatal message is prefixed with @here mention', async () => {
        process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
        nock('https://hooks.slack.com')
            .post('/hook',
                body => /<!here> .+/.test(body.text),
                {
                    reqheaders: {
                        'Content-Type': 'application/json'
                    }
                })
            .reply(200)
        const event = {level: 'fatal', details: {Project: 'Ninshubur'}}

        await expect(index.handler(event)).resolves.toBeTruthy()
    })
})

describe('Configuration', () => {
    describe('Proxy', () => {
        it('a user can override Slack host', async () => {
            process.env.SLACK_HOOK = 'https://github.com/hook'
            nock('https://github.com')
                .post('/hook',
                    _ => true,
                    {
                        reqheaders: {
                            'Content-Type': 'application/json'
                        }
                    })
                .reply(200)
            const event = {details: {Project: 'Ninshubur'}}

            await expect(index.handler(event)).resolves.toBeTruthy()
        })

        it('a user can override Slack host and port', async () => {
            process.env.SLACK_HOOK = 'https://localhost:8443/hook'
            nock('https://localhost:8443')
                .post('/hook',
                    _ => true,
                    {
                        reqheaders: {
                            'Content-Type': 'application/json'
                        }
                    })
                .reply(200)
            const event = {details: {Project: 'Ninshubur'}}

            await expect(index.handler(event)).resolves.toBeTruthy()
        })
    })
})

describe('Validation', () => {
    it('hook is required to be a valid URL', async () => {
        process.env.SLACK_HOOK = '/hook'
        const event = {details: {Project: 'Ninshubur'}}

        await expect(index.handler(event)).rejects.toBe('/hook is not a valid URL')
    })

    it('event is required', async () => {
        process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'

        await expect(index.handler()).rejects.toBe('Event is required')
    })

    it('details are required', async () => {
        process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
        const event = {}

        await expect(index.handler(event)).rejects.toBe('Details are required')
    })

    it('at least one detail is required', async () => {
        process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
        const event = {details: {}}

        await expect(index.handler(event)).rejects.toBe('At least one detail is required')
    })

    it('unknown log levels are rejected', async () => {
        process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
        const event = {level: 'foo', details: {Project: 'Ninshubur'}}

        await expect(index.handler(event)).rejects.toBe("Only 'info', 'warning', 'error' and 'fatal' log levels are supported")
    })
})