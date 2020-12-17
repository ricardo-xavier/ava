package br.com.avancoinfo.integral;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Bean;
@SpringBootApplication
public class IntegralApplication {
	
	private static final String VERSAO = "v1.5.3";
	
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

    
	@Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("*")
				.allowedMethods("GET, HEAD, PUT, PATCH, POST, DELETE, OPTIONS")				
				.allowedHeaders("*");
            }
        };
	}
    

	public static String getVersao() {
		return VERSAO;
	}

}
