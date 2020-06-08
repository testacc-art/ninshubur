const index = require('./index')
const messages = require('./messages')
const colors = require('./colors')

const nock = require('nock')
const color_names = require('color-namer')

beforeEach(() => {
    delete process.env.SLACK_HOOK
    delete process.env.NAME
})

describe('Main', () => {
    it('performs a call to Slack API', async () => {
        process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
        nock('https://hooks.slack.com')
            .post('/hook',
                {
                    username: /.+/,
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

        await expect(index.handler(event)).rejects.toBe('Slack API responded with 400: invalid_payload')
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

describe('Colors', () => {
    it('default color is info', async () => {
        process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
        nock('https://hooks.slack.com')
            .post('/hook',
                body => body.attachments.map(e => e.color).every(c => c === colors.get('info')),
                {
                    reqheaders: {
                        'Content-Type': 'application/json'
                    }
                })
            .reply(200)
        const event = {details: {Project: 'Ninshubur'}}

        await expect(index.handler(event)).resolves.toBeTruthy()
    })

    it('info color is greed', async () => {
        process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
        nock('https://hooks.slack.com')
            .post('/hook',
                body => body.attachments.map(e => e.color).every(c => color_names(c).basic[0].name === 'green'),
                {
                    reqheaders: {
                        'Content-Type': 'application/json'
                    }
                })
            .reply(200)
        const event = {level: 'info', details: {Project: 'Ninshubur'}}

        await expect(index.handler(event)).resolves.toBeTruthy()
    })

    it('warning color is yellow', async () => {
        process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
        nock('https://hooks.slack.com')
            .post('/hook',
                body => body.attachments.map(e => e.color).every(c => color_names(c).basic[0].name === 'gold'),
                {
                    reqheaders: {
                        'Content-Type': 'application/json'
                    }
                })
            .reply(200)
        const event = {level: 'warning', details: {Project: 'Ninshubur'}}

        await expect(index.handler(event)).resolves.toBeTruthy()
    })

    it('error color is red', async () => {
        process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
        nock('https://hooks.slack.com')
            .post('/hook',
                body => body.attachments.map(e => e.color).every(c => color_names(c).basic[0].name === 'red'),
                {
                    reqheaders: {
                        'Content-Type': 'application/json'
                    }
                })
            .reply(200)
        const event = {level: 'error', details: {Project: 'Ninshubur'}}

        await expect(index.handler(event)).resolves.toBeTruthy()
    })

    it('fatal color is brown', async () => {
        process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
        nock('https://hooks.slack.com')
            .post('/hook',
                body => body.attachments.map(e => e.color).every(c => color_names(c).basic[0].name === 'brown'),
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

        it('a user can override Slack host. port and protocol', async () => {
            process.env.SLACK_HOOK = 'http://localhost:8080/hook'
            nock('http://localhost:8080')
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
    describe("Name", () => {
        it('has a default', async () => {
            process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
            nock('https://hooks.slack.com')
                .post('/hook',
                    body => body.username === '𒀭𒊩𒌆𒋚',
                    {
                        reqheaders: {
                            'Content-Type': 'application/json'
                        }
                    })
                .reply(200)
            const event = {details: {Project: 'Ninshubur'}}

            await expect(index.handler(event)).resolves.toBeTruthy()
        })

        it('empty name is treated as missing', async () => {
            process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
            process.env.NAME = ''
            nock('https://hooks.slack.com')
                .post('/hook',
                    body => body.username === '𒀭𒊩𒌆𒋚',
                    {
                        reqheaders: {
                            'Content-Type': 'application/json'
                        }
                    })
                .reply(200)
            const event = {details: {Project: 'Ninshubur'}}

            await expect(index.handler(event)).resolves.toBeTruthy()
        })

        it('a user can specify a custom Slack notification username', async () => {
            process.env.SLACK_HOOK = 'https://hooks.slack.com/hook'
            process.env.NAME = 'Geronimo'
            nock('https://hooks.slack.com')
                .post('/hook',
                    body => body.username === 'Geronimo',
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