module.exports = {

    write_input: function write_input(proc, json) {

        var numPrms = num_prms(json)
        // escreve o numero de parametros na entrada padrao do processo
        proc.stdin.write(numPrms + '\n')
        write_prms(proc, json)

    }
}

// calcula o numero de parametros
function num_prms(json) {

    var numPrms = 0
    for (var key in json) {
        if (json.hasOwnProperty(key)) {
            const value = json[key]
            if ( !value || (value === "") || (value == "null") || (value.length === 0)) {
                continue
            }
            if (value instanceof Object) {
                numPrms += num_prms(value)
            } else {
                numPrms++
            }
        }
    }
    return numPrms
}

// escreve os parametros na entrada padrao do processo, no formato
// -nome
// valor
function write_prms(proc, json) {

    for (var key in json) {
        if (json.hasOwnProperty(key)) {

            var value = json[key]
            if ( !value || (value === "") || (value == "null") || (value.length === 0)) {
                continue
            }

            if (Array.isArray(value)) {
                //TODO por enquanto trata somente array de primitivos
                var aux=""
                for (var i=0; i<value.length; i++) {
                    if (i > 0) {
                        aux += ","
                    }
                    aux += value[i]
                }
                value = aux
            }

            if (value instanceof Object) {
                write_prms(proc, value)

            } else {
                proc.stdin.write("-" + key + "\n")
                proc.stdin.write(value + "\n")
            }
        }
    }
}
