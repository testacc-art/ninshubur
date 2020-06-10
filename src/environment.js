exports.hook = () => {
    const hook = process.env.SLACK_HOOK
    try {
        return Promise.resolve(new URL(hook))
    } catch (e) {
        return Promise.reject(`${hook} is not a valid URL`)
    }
}

exports.name = () => process.env.NAME ? process.env.NAME : '𒀭𒊩𒌆𒋚'

exports.avatar_url = () => process.env.AVATAR_URL ? process.env.AVATAR_URL : 'https://raw.githubusercontent.com/artamonovkirill/ninshubur/master/ninshubur.jpg'