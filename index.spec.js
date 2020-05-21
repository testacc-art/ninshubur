const index = require('./index')

describe('Main', () => {
    it('resolves', async () => {
        await expect(index.handler()).resolves.toBe('done')
    })
})