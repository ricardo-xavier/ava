package br.com.avancoinfo.redir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Redir {
	
	private static Logger logger = Logger.getLogger("redir");
	
	public static void main(String[] args) {
		
		logger.info("redir v1.0.0");
		if (args.length < 3) {
			System.err.println("Uso: redir <servidor> <porta> <id_cliente> [-d]");
			return;
		}
		if ((args.length > 3) && args[3].equals("-d")) {
			logger.setLevel(Level.FINE);
			ConsoleHandler handler = new ConsoleHandler();
	        handler.setLevel(Level.FINE);
	        logger.addHandler(handler);			
		}
		
		String servidor = args[0];
		int porta = Integer.parseInt(args[1]);
		int id = Integer.parseInt(args[2]);
		logger.info("servidor: " + servidor);
		logger.info("porta: " + porta);
		logger.info("id: " + id);
		
		String cobCpy = System.getenv("COBCPY");
    	if (cobCpy == null) {
    		System.err.println("COBCPY nao definida");
    		return;
    	}
    	
    	while (true) {

    		try {
    			
    			// conecta
    			logger.info("conectando...");
    			Socket sock = new Socket(servidor, porta);
    			
    			// envia o id do cliente
    			String s = String.format("%09d", id);
    			sock.getOutputStream().write(s.getBytes(), 0, 9);

    			// recebe o status enviado pelo servidor
    			int ret = sock.getInputStream().read();
    			if (ret != 0) {
    				logger.info("ERRO #" + ret);
    				String erro = recebeMensagem(sock.getInputStream());
    				logger.info(erro);
    				sock.close();
    				pausa();
    				continue;
    			}
    			
    			// recebe comandos
    			while (true) {
    				
    				String programa = recebeMensagem(sock.getInputStream());
    				if (programa.equals("exit")) {
    					sock.close();
    					break;
    				}
    				logger.info("programa: " + programa);
    				
    				String json = recebeMensagem(sock.getInputStream());
    				logger.fine(json);
    				
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
    		    		String erro = "ERRO Configuracao nao encontrada: " + programa.toUpperCase() + ".CPY";
    		    		logger.info(erro);
    		    		enviaMensagem(sock.getOutputStream(), erro);
    		    		sock.close();
    		    		break;
    		    	}
    		    	
    	        	List<String> formatacao = new ArrayList<String>();
    	        	BufferedReader reader = new BufferedReader(new FileReader(fCpy));
    	        	String linha;
    	        	while ((linha = reader.readLine()) != null) {
    	           		logger.fine(linha.toLowerCase());
    	        		formatacao.add(linha.toLowerCase());
    	        	}
    	        	reader.close();
    	        	
    	        	// interpreta o json de entrada - gera uma mapa com os argumentos
    	        	JsonElement entrada = JsonParser.parseString(json);
    	        	JsonObject objEntrada = entrada.getAsJsonObject();
    	        	Set<Entry<String, JsonElement>> mapa = objEntrada.entrySet();
    	        	
    	        	// monta argumentos
    	        	Iterator<Entry<String, JsonElement>> it = mapa.iterator();
    	        	args = new String[2];
    	        	args[0] = "cblapi";
    	        	args[1] = programa;
    	        	
    	        	Process proc = Runtime.getRuntime().exec(args);
    	        	
    	        	// passa os argumentos pela entrada padrao
    	        	PrintStream entradaPadrao = new PrintStream(proc.getOutputStream());
    	        	entradaPadrao.println(mapa.size() * 2);
    	       		logger.fine("write: " + mapa.size() * 2);
    	        	while (it.hasNext()) {
    	        		Entry<String, JsonElement> arg = it.next();
    	        		entradaPadrao.println("-" + arg.getKey());
    	        		entradaPadrao.println(arg.getValue().toString().replace("\"", ""));
    	        		logger.fine("write: " + "-" + arg.getKey());
    	        		logger.fine("write: " + arg.getValue().toString().replace("\"", ""));
    	        	}
    	        	entradaPadrao.flush();
    	        	entradaPadrao.close();

    	        	// espera o retorno do processamento
    	        	try {
    					proc.waitFor();
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    		    		String erro = "ERRO na execucao: " + e.getMessage();
    		    		logger.info(erro);
    		    		enviaMensagem(sock.getOutputStream(), erro);
    		    		sock.close();
    		    		break;
    					
    				}
    	        	ret = proc.exitValue();
    	       		logger.info("exit: " + ret);
    	       		
    	       		// recupera a resposta
    	        	if (ret == 0) {
    	        		reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    	        	} else {
    	        		reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
    	        	}
    	        	List<String> resposta = new ArrayList<String>();
    	        	while ((linha = reader.readLine()) != null) {
    	           		logger.fine(linha);
    	            	resposta.add(linha);
    	        	}
    	        	reader.close();        	

    	        	// formata o json com a resposta
    	        	json = new Json().toJson(formatacao, resposta);
    	       		logger.fine(json);
    				
    	       		// envia a resposta
    	       		enviaMensagem(sock.getOutputStream(), json);

    			}
    		
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		
    		pausa();
    	}
		
	}

	private static void pausa() {
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
		}
	}

	private static String recebeMensagem(InputStream inputStream) throws IOException {
		int n = inputStream.read();
		byte[] bytes = inputStream.readNBytes(n);
		return new String(bytes);
	}
	

	private static void enviaMensagem(OutputStream outputStream, String mensagem) throws IOException {
		int n = mensagem.length();
		String s = String.format("%09d", n);
		outputStream.write(s.getBytes(), 0, 9);
		outputStream.write(mensagem.getBytes(), 0, n);
	}	

}
