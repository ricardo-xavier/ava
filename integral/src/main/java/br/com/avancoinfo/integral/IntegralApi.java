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

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
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
    	logger.info("hello: API integral " + IntegralApplication.getVersao());
        return "API integral " + IntegralApplication.getVersao();
    }
    
    // chamado pelo GET e pelo POST
    private ResponseEntity<String> executa(String programa, String[] prms, Map<String, String[]> filhos) {

    	try {
    		
    		Logger logger = LoggerFactory.getLogger(IntegralApi.class);
    	
    		String cobCpy = System.getenv("COBCPY");
    		if (cobCpy == null) {
    			String erro = "COBCPY nao definida";
    			logger.info(erro);
    			return new ResponseEntity<>(erro, HttpStatus.INTERNAL_SERVER_ERROR);
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
    			String erro = "Configuracao nao encontrada: " + programa.toUpperCase() + ".CPY";
    			logger.info(erro);
    			return new ResponseEntity<>(erro, HttpStatus.INTERNAL_SERVER_ERROR);
    		}

    		List<String> formatacao = new ArrayList<String>();
    		BufferedReader reader = new BufferedReader(new FileReader(fCpy));
    		String linha;
    		while ((linha = reader.readLine()) != null) {
    			logger.debug(linha.toLowerCase());
    			formatacao.add(linha.toLowerCase());
    		}
    		reader.close();
    	
    		// passagem pela entrada padrao
    		String[] args = new String[2];
    		args[0] = "cblapi";
    		args[1] = programa;
    		Process proc = Runtime.getRuntime().exec(args);
    	
    		PrintStream writer = new PrintStream(proc.getOutputStream());
    		
    		if (prms.length == 1) {
    			String[] nomeValor = prms[0].split("=");
    			if ((nomeValor.length == 2) || nomeValor[1].startsWith("{")) {
    				try {
    					filhos = new HashMap<String, String[]>();
    					prms = getPrmsFromJson(nomeValor[1], filhos);
    				} catch (Exception e) {
    					e.printStackTrace();
    					return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    				}
    		       	
    			}
    		}

    		int numPrms = prms.length;
    		if (filhos != null) {
    			Iterator<String> it = filhos.keySet().iterator();
    			while (it.hasNext()) {
    				String pai = it.next();
    				String[] prmsFilhos = filhos.get(pai);
    				if (prmsFilhos != null) {
    					numPrms += prmsFilhos.length - 1;
    				}
    			}
    		}
    		writer.println(numPrms);
    		logger.debug("write: " + numPrms);
    		
    		for (String prm : prms) {
    			String[] nomeValor = prm.split("=");
    			if ((nomeValor.length == 1) || nomeValor[1].equals("null")) { 
    				logger.debug("nulo: " + "-" + nomeValor[0]);
    				continue;
    			}
    			if (nomeValor.length > 1 && nomeValor[1].startsWith("#")) {
    				String pai = nomeValor[1].substring(1);
    				String[] prmsFilhos = filhos.get(pai);
    	    		for (String prmFilho : prmsFilhos) {
    	    			String[] nomeValorFilho = prmFilho.split("=");
    	    			if ((nomeValorFilho.length == 1) || nomeValorFilho[1].equals("null")) { 
    	    				logger.debug("nulo: " + "-" + nomeValorFilho[0]);
    	    				continue;
    	    			} else {
    	    				writer.println("-" + nomeValorFilho[0]);
    	    				writer.println(nomeValorFilho.length > 1 ? nomeValorFilho[1] : "");
    	    				logger.debug("write: " + "-" + nomeValorFilho[0]);
    	    				logger.debug("write: " + (nomeValorFilho.length > 1 ? nomeValorFilho[1] : ""));
    	    			}
    	    		}
    				
    			} else {
    				writer.println("-" + nomeValor[0]);
    				writer.println(nomeValor.length > 1 ? nomeValor[1] : "");
    				logger.debug("write: " + "-" + nomeValor[0]);
    				logger.debug("write: " + (nomeValor.length > 1 ? nomeValor[1] : ""));
    			}
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

    		String json = new Json().toJson(formatacao, resposta);
    		logger.debug(json);

    		HttpHeaders responseHeaders = new HttpHeaders();
    	    //responseHeaders.set("Access-Control-Allow-Headers", "authorization");
    	    //responseHeaders.set("Access-Control-Allow-Methods", "GET,HEAD,PUT,PATCH,POST,DELETE");
    	    responseHeaders.set("Access-Control-Allow-Origin", "");
    	    
    	    return ResponseEntity.ok()
    	    	      .headers(responseHeaders)
    	    	      .body(json.toString());
        
    	} catch (Exception e) {
    		e.printStackTrace();
    		return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    	}

    }
    
    @GetMapping("/executa/{programa}")
    public ResponseEntity<String> executa(@PathVariable("programa") String programa, HttpServletRequest request) {

		@SuppressWarnings("deprecation")
		String queryString = java.net.URLDecoder.decode(request.getQueryString());

    	Logger logger = LoggerFactory.getLogger(IntegralApi.class);
    	logger.info("executa GET : " + programa);
    	logger.info(queryString);

		String[] prms = queryString.split("&");
		return executa(programa, prms, null);
    }
    
    @GetMapping("/executa/{diretorio}/{programa}")
    public ResponseEntity<String> executaDiretorio(@PathVariable("diretorio") String diretorio,
    		@PathVariable("programa") String programa, 
    		HttpServletRequest request) {

		@SuppressWarnings("deprecation")
		String queryString = java.net.URLDecoder.decode(request.getQueryString());

    	Logger logger = LoggerFactory.getLogger(IntegralApi.class);
    	logger.info("executa GET : " + programa);
    	logger.info(queryString);

		String[] prms = queryString.split("&");
		return executa(programa, prms, null);
    }

    @PostMapping("/executa/{programa}")
    public ResponseEntity<String> executa(@PathVariable("programa") String programa, @RequestBody String json) {
    	
    	Logger logger = LoggerFactory.getLogger(IntegralApi.class);
       	logger.info("executa POST : " + programa);
       	logger.info(json);

		try {
			Map<String, String[]> filhos = new HashMap<String, String[]>();
			String[] prms = getPrmsFromJson(json, filhos);
			return executa(programa, prms, filhos);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }

    private String[] getPrmsFromJson(String json, Map<String, String[]> filhos) throws Exception {
    	
    	JsonElement entrada = JsonParser.parseString(json);
    	JsonObject objEntrada = entrada.getAsJsonObject();
    	Set<Entry<String, JsonElement>> mapa = objEntrada.entrySet();
    	String[] prms = new String[mapa.size()];
    	Iterator<Entry<String, JsonElement>> it = mapa.iterator();
    	int i = 0;
    	while (it.hasNext()) {
    		Entry<String, JsonElement> arg = it.next();
    		String valor = arg.getValue().toString();
    		JsonElement elemento = arg.getValue();
    		if (elemento != null) {
    			if (elemento.isJsonArray()) {
    				valor = valor.trim();
    				valor = valor.substring(1, valor.length()-1);
    			}
    			if (elemento.isJsonObject()) {
   					String[] prms2 = getPrmsFromJson(valor, null);
   					if (filhos != null) {
   						filhos.put(arg.getKey(), prms2);
   					}
   		    		prms[i] = arg.getKey() + "=" + "#" + arg.getKey();
   		    		i++;
   		    		continue;
    			}
    		}
    		prms[i] = arg.getKey() + "=" + valor.replace("\"", "");
    		i++;
    	}
    	return prms;
    	
	}

	@PostMapping("/executa/{id}/{programa}")
    public ResponseEntity<String> executaId(@PathVariable("id") int id, @PathVariable("programa") String programa, @RequestBody String json) {
    	
    	Logger logger = LoggerFactory.getLogger(IntegralApi.class);
    	try {

        	logger.info("executa: " + id + " " + programa);
        	logger.info(json);
        	
        	Socket sock = sockets.get(id);
        	if (sock == null) {
        		return new ResponseEntity<>("Agente " + id + " nao conectado", HttpStatus.INTERNAL_SERVER_ERROR);
        	}

        	Comunicacao.enviaMensagem(sock.getOutputStream(), programa);
        	Comunicacao.enviaMensagem(sock.getOutputStream(), json);

        	json = Comunicacao.recebeMensagem(sock.getInputStream());
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
