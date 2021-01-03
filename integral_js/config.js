
module.exports = {

    find_cpy: function find_cpy() {

        const cobcpy=process.env.COBCPY;
        if (cobcpy === undefined) {
            return [ 1, 'COBCPY nao definida' ]
        }
        const dirs=cobcpy.split(':');

        //TODO extrair da requisicao
        const program='WSCOBOL';

        const fs = require('fs')
        for (i=0; i<dirs.length; i++) {
            const path = dirs[i] + '/' + program + '.CPY';
            if (fs.existsSync(path)) {
                return [ 0, path ]
            }
        }

        return [ 2, program + '.CPY nao encontrado' ]

    }

}

