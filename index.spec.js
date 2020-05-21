const nock = require('nock')

const index = require('./index')

beforeEach(() => {
    delete process.env.SLACK_HOOK
})

describe('Main', () => {
    it('performs a call to Slack API', async () => {
        process.env.SLACK_HOOK = '/hook'
        nock('https://hooks.slack.com')
            .post('/hook',
                {
                    text: 'test'
                },
                {
                    reqheaders: {
                        'Content-Type': 'application/json'
                    }
                })
            .reply(200, 'success')

        await expect(index.handler())
            .resolves.toBe('Successfully notified about {"text":"test"}')
    })

    it('fails on 4xx responses', async () => {
        process.env.SLACK_HOOK = '/hook'
        nock('https://hooks.slack.com')
            .post('/hook',
                {
                    text: 'test'
                },
                {
                    reqheaders: {
                        'Content-Type': 'application/json'
                    }
                })
            .reply(400, 'invalid_payload')

        await expect(index.handler())
            .rejects.toBe('Slack API responded with 400 code')
    })
})