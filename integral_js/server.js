const http = require('http');
const config = require('./config.js');
const exec = require('./exec.js');

function send_header(res, code) {
    res.writeHead(code, { 
        'Content-Type': 'application/json' ,
        'Access-Control-Allow-Headers': '*',
        'Access-Control-Allow-Methods': '*'
    });
}

function send_error(res, msg) {

    send_header(res, 500);
    erro = { "message" : msg }
    res.write(JSON.stringify(erro));
    res.end();

}

function send_response(res, json) {

    send_header(res, 200);
    res.write(JSON.stringify(json));
    res.end();

}

server = function(req, res) {

    const [ result, cpy ] = config.find_cpy();
    if ( result !== 0 ) {
        send_error(res, 'Erro na leitura da configuracao')
        return;
    }

    exec.exec_program(res, send_response);

}

http.createServer(server).listen(8080);
