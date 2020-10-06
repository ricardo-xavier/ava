package br.com.avancoinfo.integral;

import java.util.ArrayList;
import java.util.List;

public class Json {
	
	private List<String> dados;
	private List<Item> itens;
	private int d;
	private StringBuilder json;
	
    public String toJson(List<String> formatacao, List<String> dados) {
    	
    	this.dados = dados;
    	
    	// carrega a lista de itens
    	itens = new ArrayList<>();
    	for (int f=0; f<formatacao.size(); f++) {
    		String[] args = formatacao.get(f).split("\\s+");
    		if (args.length < 3) {
    			continue;
    		}
    		int nivel = Integer.parseInt(args[1]);
    		if (nivel == 1) {
    			continue;
    		}
    		String nome = args[2];
    		if (nome.startsWith("count-")) {
    			continue;
    		}
    		Item item = new Item();
    		item.setNivel(nivel);
    		item.setNome(nome);
    		
    		if (args[3].equals("occurs")) {
    			item.setArray(true);
    		}
    		
    		if (args[3].equals("pic")) {
    			item.setElementar(true);
    			item.setPic(args[4]);
    		}
    		
    		itens.add(item);
    	}
    	
    	// seta a posicao dos filhos
    	for (int i=0; i<(itens.size()-1); i++) {
    		Item item = itens.get(i);
    		if (item.isElementar()) {
    			continue;
    		}
    		item.setIni(i+1);
    		item.setFim(i+1);
    		for (int j=i+1; j<itens.size(); j++) {
    			Item itemJ = itens.get(j);
    			if (itemJ.getNivel() <= item.getNivel()) {
    				break;
    			}
    			item.setFim(j);
    		}
    	}
    	
    	json = new StringBuilder();
    	carregaItem(0);
    	return json.toString();
    }
    
    private void carregaOcorrencia(int o, int pos) {
    	
    	Item item = itens.get(pos);

    	if (item.isElementar()) {
    		String valor = dados.get(d++);
    		item.setValor(valor);
    		String virgula = "";
    		if (pos < (itens.size()-1)) {
    			Item proximo = itens.get(pos+1);
    			if (proximo.getNivel() == item.getNivel()) {
    				virgula = ",";
    			}
    		}
    		if (item.getPic().startsWith("x")) {
    			json.append(String.format("\"%s\": \"%s\"%s%n", item.getNome(), item.getValor(), virgula));
    		} else {
    			int v = item.getPic().indexOf("v");
    			if (v < 0) {
    				json.append(String.format("\"%s\": %d%s%n", item.getNome(), Long.parseLong(item.getValor()), virgula));
    			} else {
    				int decimais = item.getPic().replace(".", "").substring(v+1).length();
    				String inteira = item.getValor().substring(0, item.getValor().length() - decimais);
    				String decimal = item.getValor().substring(item.getValor().length() - decimais);
    				String fmt = String.format("%%0%dd", decimais);
    				json.append(String.format("\"%s\": %d.%s%s%n", item.getNome(), 
    						Long.parseLong(inteira), String.format(fmt, Long.parseLong(decimal)), 
    						virgula));
    			}
    		}
    		
    	} else {
    		json.append("{\n");
    		for (int i=item.getIni(); i<=item.getFim(); i++) {
    			i = carregaItem(i);
    		}
    		json.append("}\n");
    	}
    }

	private int carregaItem(int pos) {

		Item item = itens.get(pos);
		if (item.isArray()) {
			if (item.getNivel() == 3) {
				json.append("[\n");
			} else {
				json.append(String.format("\"%s\": [\n", item.getNome()));
			}
			int occurs = Integer.parseInt(dados.get(d++));
			item.setOccurs(occurs);
		} else {
			item.setOccurs(1);
		}
		for (int o=0; o<item.getOccurs(); o++) {
			carregaOcorrencia(o, pos);
			if (o < (item.getOccurs()-1)) {
				json.append(",\n");
			}
		}
		
		if (item.isArray()) {
			json.append("]\n");
		}		
		
		return item.isElementar() ? pos : item.getFim();
	}
}
