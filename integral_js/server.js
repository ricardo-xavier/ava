const http = require('http')
const config = require('./config.js')
const exec = require('./exec.js')
const output = require('./output.js')

var fd = null

function send_header(res, code) {
    res.writeHead(code, { 
        'Content-Type': 'application/json' ,
        'Access-Control-Allow-Headers': '*',
        'Access-Control-Allow-Methods': '*'
    })
}

function send_error(res, msg) {

    send_header(res, 500)
    erro = { "message" : msg }
    res.write(JSON.stringify(erro))
    res.end()

}

function send_response(res, code, data) {

    formated = output.format(data, fd)
    //console.log(formated)
    const json = {
        "code": code,
        "output": formated
    }
    send_header(res, 200)
    res.write(JSON.stringify(json))
    res.end()

}

server = function(req, res) {

    // pega o nome do programa cobol e os parametros de entrada na url
    const decodedUrl = "http://localhost" + decodeURIComponent(req.url)
    const url = new URL(decodedUrl)
    const args = url.pathname.split('/')
    const progname =  args[args.length - 1]
    const prms=url.searchParams.getAll("filter")

    // procura a cpy do programa no COBCPY
    // carrega a formatacao para o json de saida
    const [ result, cpy, _fd ] = config.find_cpy(progname)
    if ( result !== 0 ) {
        send_error(res, 'Erro na leitura da configuracao')
        return
    }
    fd = _fd

    // executa o programa cobol
    console.log("Executando programa %s...", progname)
    exec.exec_program(res, send_response, progname, prms, fd)

}

// processa argumentos da linha de comando
// node server.js [-p <port>]
var port = 8080 // porta default
for (var a=0; a<process.argv.length; a++) {
    if (process.argv[a] === "-p") {
        port = parseInt(process.argv[++a])
    }
}

console.log("Esperando conexoes na porta %d...", port)
http.createServer(server).listen(port)
