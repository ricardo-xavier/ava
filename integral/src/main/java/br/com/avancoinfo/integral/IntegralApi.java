package br.com.avancoinfo.integral;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping(path = "/integral")
public class IntegralApi {
	
	private int dbg = 2;

    @GetMapping
    public String hello() {
    	if (dbg > 0) {
    		System.out.println("hello: " + System.getProperty("user.dir"));
    	}
        return "API integral v0.1.0-SNAPSHOT";
    }

    @PostMapping("/executa/{programa}")
    public ResponseEntity<String> executa(@PathVariable("programa") String programa, @RequestBody String json) {
    	
    	try {
    		
        	if (dbg > 0) {
        		System.out.println("executa: " + programa);
        		System.out.println(json);
        	}
    		
    		String cobCpy = System.getenv("COBCPY");
        	if (cobCpy == null) {
        		return new ResponseEntity<>("COBCPY nao definida", HttpStatus.INTERNAL_SERVER_ERROR);
        	}
        	
        	File fCpy = null;
        	String[] cpys = cobCpy.split(":");
        	for (String dir : cpys) {
        		String cpy = dir + "/" + programa.toUpperCase() + ".CPY";
        		fCpy = new File(cpy);
        		if (fCpy.exists()) {
        			break;
        		}
        		fCpy = null;
        	}
        	if (fCpy == null) {
        		return new ResponseEntity<>("Configuracao nao encontrada: " + programa.toUpperCase() + ".CPY", HttpStatus.INTERNAL_SERVER_ERROR);
        	}
        	
        	List<String> formatacao = new ArrayList<String>();
        	BufferedReader reader = new BufferedReader(new FileReader(fCpy));
        	String linha;
        	while ((linha = reader.readLine()) != null) {
            	if (dbg > 1) {
            		System.out.println(linha.toLowerCase());
            	}        		
        		formatacao.add(linha.toLowerCase());
        	}
        	reader.close();
        	
        	JsonElement entrada = JsonParser.parseString(json);
        	JsonObject objEntrada = entrada.getAsJsonObject();

        	StringBuilder cmd = new StringBuilder();
        	cmd.append("cblapi " + programa);
        	Set<Entry<String, JsonElement>> mapa = objEntrada.entrySet();
        	
        	Iterator<Entry<String, JsonElement>> it = mapa.iterator();
        	int nargs = mapa.size() * 2 + 2;
        	String[] args = new String[nargs];
        	args[0] = "cblapi";
        	args[1] = programa;
        	int a = 2;
        	while (it.hasNext()) {
        		Entry<String, JsonElement> arg = it.next();
        		cmd.append(" -" + arg.getKey() + " " + arg.getValue());
        		args[a++] = "-" + arg.getKey();
        		args[a++] = arg.getValue().toString().replace("\"", "");
        	}
        	if (dbg > 0) {
        		System.out.println("cmd: " + cmd.toString());
        	}
        	
        	Process proc = Runtime.getRuntime().exec(args);
        	proc.waitFor();
        	int ret = proc.exitValue();
        	if (dbg > 0) {
        		System.out.println("exit: " + ret);
        	}
        	if (ret == 0) {
        		reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        	} else {
        		reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        	}
        	List<String> resposta = new ArrayList<String>();
        	while ((linha = reader.readLine()) != null) {
            	if (dbg > 1) {
            		System.out.println(linha);
            	}
            	resposta.add(linha);
        	}
        	reader.close();        	

        	json = new Json().toJson(formatacao, resposta);
        	if (dbg > 1) {
        		System.out.println(json);
        	}        	
	        return new ResponseEntity<>(json.toString(), HttpStatus.OK);
			
		} catch (Exception e) {
			e.printStackTrace();
        	if (dbg > 0) {
        		System.out.println("INTERNAL_SERVER_ERROR:" + e.getMessage());
        	}        				
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
    
    
}
