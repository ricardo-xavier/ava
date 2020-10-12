package br.com.avancoinfo.redir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Redir {
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		
		//FIXME
		Logger logger = Logger.getLogger("redir");
		
		String servidor = args[0];
		int porta = Integer.parseInt(args[1]);
		int id = Integer.parseInt(args[2]);
		
		String cobCpy = System.getenv("COBCPY");
    	if (cobCpy == null) {
    		System.out.println("COBCPY nao definida");
    		return;
    	}
    			
		Socket sock = new Socket(servidor, porta);
		String s = String.format("%09d", id);
		sock.getOutputStream().write(s.getBytes(), 0, 9);
		
		int ret = sock.getInputStream().read();
		System.out.println(ret);
		if (ret != 0) {
			//TODO
		}
		
		while (true) {
		
			int n = sock.getInputStream().read();
			if (n <= 0) {
				break;
			}
			logger.info("n=" + n);
			
			byte[] resp = new byte[n];
			sock.getInputStream().read(resp, 0, n);
			String programa = new String(resp);
			logger.info("programa=" + programa);
			
			n = sock.getInputStream().read();
			logger.info("n=" + n);
			resp = new byte[n];
			sock.getInputStream().read(resp, 0, n);
			String json = new String(resp);
			logger.info("json=" + json);
			
			//TODO cache com a formatacao do programa
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
	    		String msg = "Configuracao nao encontrada: " + programa.toUpperCase() + ".CPY";
				n = msg.length();
				sock.getOutputStream().write(n);
				sock.getOutputStream().write(msg.getBytes(), 0, n);
	    		continue;
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
        	args = new String[2];
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

        	try {
				proc.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	ret = proc.exitValue();
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
			
       		n = json.length();
    		s = String.format("%09d", n);
    		sock.getOutputStream().write(s.getBytes(), 0, 9);
			sock.getOutputStream().write(json.getBytes(), 0, n);

		}
		
		sock.close();
	}

}
