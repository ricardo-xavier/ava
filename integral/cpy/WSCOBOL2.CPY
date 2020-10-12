       01 json-wscobol.
          03 count-notas          pic 9(03).
          03 notas occurs 100 times.
             05 nf                pic 9(10).
             05 emissao           pic 9(06).
             05 valor             pic 9(09)v99.
             05 count-itens       pic 9(03).
             05 itens occurs 100 times.
                07 quantidade     pic 9(09)v9999.
                07 descricao      pic x(35).
