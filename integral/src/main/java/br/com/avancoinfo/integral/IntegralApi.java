package br.com.avancoinfo.integral;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static Map<Integer, Socket> sockets = new HashMap<Integer, Socket>();

	static {
		ThreadRedir redir = new ThreadRedir();
		redir.start();
	}
	
    @GetMapping
    public String hello() {
    	Logger logger = LoggerFactory.getLogger(IntegralApi.class);
    	logger.info("hello: API integral v0.2.0-SNAPSHOT");
        return "API integral v0.2.0-SNAPSHOT";
    }

    @PostMapping("/executa/{programa}")
    public ResponseEntity<String> executa(@PathVariable("programa") String programa, @RequestBody String json) {
    	
    	Logger logger = LoggerFactory.getLogger(IntegralApi.class);
    	try {
    		
        	logger.info("executa: " + programa);
        	logger.info(json);
        	
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
           		logger.debug(linha.toLowerCase());
        		formatacao.add(linha.toLowerCase());
        	}
        	reader.close();
        	
        	JsonElement entrada = JsonParser.parseString(json);
        	JsonObject objEntrada = entrada.getAsJsonObject();

        	Set<Entry<String, JsonElement>> mapa = objEntrada.entrySet();
        	
        	Iterator<Entry<String, JsonElement>> it = mapa.iterator();
        	
        	// passagem pela entrada padrao
        	String[] args = new String[2];
        	args[0] = "cblapi";
        	args[1] = programa;
        	Process proc = Runtime.getRuntime().exec(args);
        	
        	PrintStream writer = new PrintStream(proc.getOutputStream());
        	writer.println(mapa.size() * 2);
       		logger.debug("write: " + mapa.size() * 2);
        	while (it.hasNext()) {
        		Entry<String, JsonElement> arg = it.next();
        		writer.println("-" + arg.getKey());
        		writer.println(arg.getValue().toString().replace("\"", ""));
        		logger.debug("write: " + "-" + arg.getKey());
        		logger.debug("write: " + arg.getValue().toString().replace("\"", ""));
        	}
        	writer.flush();
        	writer.close();

        	proc.waitFor();
        	int ret = proc.exitValue();
       		logger.info("exit: " + ret);
        	if (ret == 0) {
        		reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        	} else {
        		reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        	}
        	List<String> resposta = new ArrayList<String>();
        	while ((linha = reader.readLine()) != null) {
           		logger.debug(linha);
            	resposta.add(linha);
        	}
        	reader.close();        	

        	json = new Json().toJson(formatacao, resposta);
       		logger.debug(json);
	        return new ResponseEntity<>(json.toString(), HttpStatus.OK);
			
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }


    @PostMapping("/executa/{id}/{programa}")
    public ResponseEntity<String> executaId(@PathVariable("id") int id, @PathVariable("programa") String programa, @RequestBody String json) {
    	
    	Logger logger = LoggerFactory.getLogger(IntegralApi.class);
    	try {
    		
        	logger.info("executa: " + id + " " + programa);
        	logger.info(json);
        	
        	Socket sock = sockets.get(id);
        	if (sock == null) {
        		//TODO agente nao conectado
        	}
        	
			int n = programa.length();
			sock.getOutputStream().write(n);
			sock.getOutputStream().write(programa.getBytes(), 0, n);
			n = json.length();
			sock.getOutputStream().write(n);
			sock.getOutputStream().write(json.getBytes(), 0, n);
			
			byte[] bytes = new byte[9];
			sock.getInputStream().read(bytes, 0, 9);
			n = Integer.parseInt(new String(bytes));
        	System.err.println(n);
        	logger.info("resp: " + n);
        	byte[] resp = sock.getInputStream().readNBytes(n);
        	json = new String(resp);
        	logger.info("resp: " + json);
        	
	        return new ResponseEntity<>(json.toString(), HttpStatus.OK);
			
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }

	public static Map<Integer, Socket> getSockets() {
		return sockets;
	}

	public static void setSockets(Map<Integer, Socket> sockets) {
		IntegralApi.sockets = sockets;
	}
    
}
