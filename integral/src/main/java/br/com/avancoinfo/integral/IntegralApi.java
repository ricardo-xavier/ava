package br.com.avancoinfo.integral;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/integral")
public class IntegralApi {

    @GetMapping
    public String hello() {
    	System.out.println(System.getProperty("user.dir"));
        return "API integral v0.1.0-SNAPSHOT";
    }

    @GetMapping("/executa/{programa}")
    public ResponseEntity<String> executa(@PathVariable("programa") String programa) {
    	
    	try {
    		
    		String cobCpy = System.getenv("COBCPY");
        	if (cobCpy == null) {
        		return new ResponseEntity<>("COBCPY nao definida", HttpStatus.INTERNAL_SERVER_ERROR);
        	}
        	
        	String cpy = cobCpy + "/" + programa.toUpperCase() + ".CPY";
    		File fCpy = new File(cpy);
        	if (!fCpy.exists()) {
        		return new ResponseEntity<>("Configuracao nao encontrada: " + cpy, HttpStatus.INTERNAL_SERVER_ERROR);
        	}
        	
        	List<String> formatacao = new ArrayList<String>();
        	BufferedReader reader = new BufferedReader(new FileReader(fCpy));
        	String linha;
        	while ((linha = reader.readLine()) != null) {
        		formatacao.add(linha.toLowerCase());
        	}
        	reader.close();
        	
        	String json = new Json().toJson(formatacao, Teste.respostaTeste());
	        return new ResponseEntity<>(json.toString(), HttpStatus.OK);
			
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
    
    
}
