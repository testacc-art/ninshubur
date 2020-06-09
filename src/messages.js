const messages = {
    info: [
        "It's time to kick ass and chew bubblegum... and I'm all outta gum.",
        'Magic Mirror on the wall, who is the fairest one of all?',
        'Elementary, my dear Watson.',
        "It's alive! It's alive!",
        'To infinity and beyond!',
        'Booyakasha!',
        'Battlecruiser operational.'
    ],
    error: [
        "Good men mean well. We just don't always end up doing well.",
        'Thank you Mario! But our Princess is in another castle!',
        'I used to be an adventurer like you, until I took an arrow to the knee.',
        "It's a trap",
        'Did I do that?',
        "Nobody's perfect.",
        'Houston, we have a problem.',
        "Toto, I've a feeling we're not in Kansas anymore.",
        'Finish him!',
        "He's dead, Jim",
        'Wasted',
        "Everybody's gotta learn some time",
        'All your base are belong to us',
        "Help me, Obi-Wan Kenobi. You're my only hope.",
        'I love the smell of napalm in the morning.',
        "Here's Johnny!",
        'The hive cluster is under attack!'
    ]
}

const messageLevel = (level) => level === 'warning' || level === 'fatal' ? 'error' : level
const random = (list) => list[Math.floor(Math.random() * list.length)]

exports.get = (level) => random(messages[messageLevel(level)])
exports.info = () => messages.info
exports.errors = () => messages.error