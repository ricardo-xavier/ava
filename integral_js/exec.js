const spawn = require('child_process').spawn
const input = require('./input.js')

module.exports = {

    exec_program: function exec_program(res, send_response, progname, prms, fd) {

        //console.log(fd)

        const cmd = 'cblapi ' + progname
        const child = spawn(cmd, [], {shell:true})

        var json = JSON.parse(prms[0])
        //console.log(json)
        input.write_input(child, json)

        var output = ""
        child.stdout.on('data', (data) => {
            output += data.toString()
        })

        child.on('exit', function (code, signal) {
            send_response(res, code, output)
        })
    }

}
