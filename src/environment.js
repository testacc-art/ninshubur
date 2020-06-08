exports.hook = () => {
    const hook = process.env.SLACK_HOOK
    try {
        return Promise.resolve(new URL(hook))
    } catch (e) {
        return Promise.reject(`${hook} is not a valid URL`)
    }
}

exports.name = () => process.env.NAME ? process.env.NAME : '𒀭𒊩𒌆𒋚'