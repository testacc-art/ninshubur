const colors = {
    info: '#11c560',
    warning: '#f0e963',
    error: '#c41700',
    fatal: '#b51702'
}

exports.get = level => colors[level]