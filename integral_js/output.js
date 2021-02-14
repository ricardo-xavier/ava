var json = []
var fd = ""
var array = null
var a = 0

module.exports = {

    format: function format(data, _fd) {
    
        fd = _fd
        array = data.toString().split("\n")

        format_item(0)
        return json.join("")
    }
}

function format_item(i) {

    var occurs = 1

    if (fd[i].isArray) {

        if (fd[i].level == 3) {
            json.push('[\n')
        } else {
            json.push('"' + fd[i].name.replace('-', '_') + '": [\n')
        }

        occurs = array[a++]

    }

    for (var o=0; o<occurs; o++) {
        format_occurrence(i, o)
        if (o < (occurs-1)) {
            json.push(',\n')
        }
    }

    if (fd[i].isArray) {
        json.push(']\n')
    }
}

function format_occurrence(i, o) {

    if (fd[i].isPrimitive) {

        fd[i].value = array[a++]
        var comma = ((i < (fd.length - 1)) && (fd[i].level == fd[i+1].level)) ? "," : ""

        if (fd[i].pic.startsWith("x")) {
            json.push('"' + fd[i].name.replace('-', '_') + '": "' + fd[i].value.trim() + '"' + comma + '\n')

        } else {
            var v = fd[i].pic.indexOf("v");
            if (v < 0) {
                json.push('"' + fd[i].name.replace('-', '_') + '": ' + Number(fd[i].value.trim()) + comma + '\n')

            } else {
                const decimals = fd[i].pic.replace(".", "").substring(v+1).length
                const integer = fd[i].value.substring(0, fd[i].value.length - decimals)
                const decimal = fd[i].value.substring(fd[i].value.length - decimals)
                json.push('"' + fd[i].name.replace('-', '_') + '": ' 
                    + Number(integer) + "." + Number(decimal)
                    + comma + '\n')
            }
        }

    } else {
        json.push('{\n')
        for (var j=fd[i].init; j<=fd[i].end; j++) {
            format_item(j)
        }
        json.push('}\n')
    }
}
