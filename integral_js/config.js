module.exports = {

    find_cpy: function find_cpy(program) {

        var items = []

        const cobcpy=process.env.COBCPY
        if (cobcpy === undefined) {
            return [ 1, 'COBCPY nao definida', items ]
        }
        const dirs=cobcpy.split(':')

        const fs = require('fs')
        for (i=0; i<dirs.length; i++) {
            const path = dirs[i] + '/' + program.toUpperCase() + '.CPY'
            if (fs.existsSync(path)) {
                items = load(fs, path)
                return [ 0, "ok", items ]
            }
        }

        return [ 2, program + '.CPY nao encontrado', items ]

    }

}

// carrega o CPY do programa e retorna um array de items
// exemplo de CPY /u/fontes/cpys/WSCOBOL3.CPY:
//       01 json-wscobol.
//             03 count-lojas          pic 9(03).
//             03 lojas occurs 100 times.
//                05 codigo-filial     pic 9(02).
//       ...
function load(fs, path) {

    var items = []

    const data = fs.readFileSync(path, 'utf8')
    const array = data.toString().split("\n")
    const regex = new RegExp("\\s+")

    for (i in array) {

        const line = array[i]

        if (line.includes("count-")) {
            continue
        }

        const args = line.toLowerCase().split(regex)
        if (args.length < 4) {
            continue
        }

        item = {
            "level": parseInt(args[1]),
            "name": args[2],
            "isArray": false,
            "isPrimitive": false,
            "pic": "",
            "init": -1,
            "end": -1
        }

        if (args[3] === "occurs") {
            item.isArray = true
        } else if (args[3] === "pic") {
            item.isPrimitive = true
            item.pic = args[4]
        }

        items.push(item)

    }

    // seta a posicao dos filhos
    for (var i=0; i<(items.length-1); i++) {
        
        if (items[i].isPrimitive) {
            continue;
        }

        items[i].init = i + 1
        items[i].end = i + 1

        for (var j=i+1; j<items.length; j++) {
            
            if (items[j].level <= items[i].level) {
                break;
            }

            items[i].end = j
        }
    }

    return items
}
