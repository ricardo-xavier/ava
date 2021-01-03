const spawn = require('child_process').spawn;

module.exports = {

    exec_program: function exec_program(res, send_response) {

        var output = "";

        //TODO programa
        const cmd = '/home/ricardo/shell/cblapi ' + 'wscobol.int';
        const child = spawn(cmd, [], {shell:true});

        //TODO passar parametros de entrada
        child.stdin.write('/tmp\n');
        child.stdin.write('/etc\n');

        //TODO receber saida de forma sincrona
        child.stdout.on('data', (data) => {
            output += data.toString();
        });

        child.on('exit', function (code, signal) {
            const json = {
                "code": code,
                "output": output
            };
            send_response(res, json);
        });
    }

}
