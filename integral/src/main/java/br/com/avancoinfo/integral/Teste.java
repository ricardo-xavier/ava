package br.com.avancoinfo.integral;

import java.util.ArrayList;
import java.util.List;

public class Teste {
    
    public static void main(String[] args) {
		new IntegralApi().executa("web600", "");
	}
    
	public static List<String> respostaTeste() {
		List<String> resposta = new ArrayList<String>();
		resposta.add("002");
		resposta.add("0000138376");
		resposta.add("270820");
		resposta.add("00000598340");
		resposta.add("003");
		resposta.add("0000004000000");
		resposta.add("AMIDO MILHO MAIZENA 500GR          ");
		resposta.add("0000000800000");
		resposta.add("AMIDO MILHO MAIZENA 1KG            ");
		resposta.add("0000017000000");
		resposta.add("AMIDO MILHO MAIZENA 200GR          ");
		resposta.add("0000050572");
		resposta.add("030920");
		resposta.add("00000025551");
		resposta.add("004");
		resposta.add("0000000000000");
		resposta.add("");
		resposta.add("0000004000000");
		resposta.add("AMIDO MILHO MAIZENA 500GR          ");
		resposta.add("0000000800000");
		resposta.add("AMIDO MILHO MAIZENA 1KG            ");
		resposta.add("0000017000000");
		resposta.add("AMIDO MILHO MAIZENA 200GR          ");		
		return resposta;
	}

    /*
	private List<String> respostaTeste() {
		List<String> resposta = new ArrayList<String>();
		resposta.add("0000138376");
		resposta.add("270820");
		resposta.add("00000598340");
		resposta.add("0000004000000");
		resposta.add("AMIDO MILHO MAIZENA 500GR          ");
		resposta.add("0000000800000");
		resposta.add("AMIDO MILHO MAIZENA 1KG            ");
		resposta.add("0000017000000");
		resposta.add("AMIDO MILHO MAIZENA 200GR          ");
		resposta.add("#EOL");
		resposta.add("0000050572");
		resposta.add("030920");
		resposta.add("00000025551");
		resposta.add("0000000000000");
		resposta.add("");                  
		resposta.add("0000004000000");
		resposta.add("AMIDO MILHO MAIZENA 500GR          ");
		resposta.add("0000000800000");
		resposta.add("AMIDO MILHO MAIZENA 1KG            ");
		resposta.add("0000017000000");
		resposta.add("AMIDO MILHO MAIZENA 200GR          ");
		resposta.add("#EOL");
		resposta.add("#EOL");
		resposta.add("#EOF");		
		return resposta;
	}
	*/

}
