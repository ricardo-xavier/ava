package br.com.avancoinfo.integral;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IntegralApplication {
	
	private static final String VERSAO = "v1.4.0";

	public static void main(String[] args) {
		
		SpringApplication app = new SpringApplication(IntegralApplication.class);
		
		for (int a=0; a<args.length; a++) {
			if (args[a].equals("-p")) {
				String portaTomcat = args[++a];
		        app.setDefaultProperties(Collections.singletonMap("server.port", portaTomcat));
				
			} else if (args[a].equals("-r")) {
				String portaRedir = args[++a];
				ThreadRedir.setPorta(Integer.parseInt(portaRedir));
			
			} else if (args[a].equals("-v")) {	
				System.out.println(VERSAO);
				return;
			
			} else if (args[a].equals("-h")) {
				System.out.println("Uso: java -jar integral.jar [-p <porta tomcat>] [-r <porta redir>]");
				System.out.println("debug:");
				System.out.println("java -Dlogging.level.br.com.avancoinfo=DEBUG -jar integral.jar ...");
				return;
			}
			
		}
		System.out.println("API integral " + IntegralApplication.getVersao());
        app.run(args);
	}

	public static String getVersao() {
		return VERSAO;
	}

}
