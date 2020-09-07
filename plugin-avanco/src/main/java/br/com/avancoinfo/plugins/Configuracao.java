package com.example.tutorial.plugins;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class Configuracao {

    private String listaUsuarios;
    private String listaEventosInicio;
    private String listaStatusInicio;
    private String listaEventosFim;
    private String mensagemInicio;
    private String mensagemFim;
    
	public Configuracao() {
	    listaUsuarios = "teste.plugin";
	    listaEventosInicio = "11";
	    listaStatusInicio = "3";
	    listaEventosFim = "12";
	    mensagemInicio = "Registro de trabalho iniciado pelo plugin avanço";
	    mensagemFim = "Trabalho registrado pelo plugin avanço";
	}
	
	public void carrega(PrintStream log) throws IOException {
		
        BufferedReader cfg = new BufferedReader(new FileReader("/u/atlassian/jira/plugin-avanco.cfg"));
        String linha;
        while ((linha = cfg.readLine()) != null) {
        	int p = linha.indexOf('=');
        	if (p < 0) {
        		continue;
        	}
            if (linha.startsWith("usuarios=")) {
                listaUsuarios = linha.substring(p+1).trim() + ":";
                
            } else if (linha.startsWith("eventos_inicio=")) {
                listaEventosInicio = linha.substring(p+1).trim() + ":";
                
            } else if (linha.startsWith("status_inicio=")) {
                listaStatusInicio = linha.substring(p+1).trim() + ":";
                
            } else if (linha.startsWith("eventos_fim=")) {
                listaEventosFim = linha.substring(p+1).trim() + ":";
                
            } else if (linha.startsWith("mensagem_inicio=")) {
                mensagemInicio = linha.substring(p+1).trim();
                
            } else if (linha.startsWith("mensagem_fim=")) {
                mensagemFim = linha.substring(p+1).trim();
            }
        }
        cfg.close();
        
        if (log != null) {
        	log.println(this);
        }
		
	}
	
	@Override
	public String toString() {
		return "Configuracao:"
				+ "\n  listaUsuarios     =" + listaUsuarios 
				+ "\n  listaEventosInicio=" + listaEventosInicio
				+ "\n  listaStatusInicio =" + listaStatusInicio 
				+ "\n  listaEventosFim   =" + listaEventosFim
				+ "\n  mensagemInicio    =" + mensagemInicio 
				+ "\n  mensagemFim       =" + mensagemFim;
	}

	public String getListaUsuarios() {
		return listaUsuarios;
	}

	public void setListaUsuarios(String listaUsuarios) {
		this.listaUsuarios = listaUsuarios;
	}

	public String getListaEventosInicio() {
		return listaEventosInicio;
	}

	public void setListaEventosInicio(String listaEventosInicio) {
		this.listaEventosInicio = listaEventosInicio;
	}

	public String getListaStatusInicio() {
		return listaStatusInicio;
	}

	public void setListaStatusInicio(String listaStatusInicio) {
		this.listaStatusInicio = listaStatusInicio;
	}

	public String getListaEventosFim() {
		return listaEventosFim;
	}

	public void setListaEventosFim(String listaEventosFim) {
		this.listaEventosFim = listaEventosFim;
	}

	public String getMensagemInicio() {
		return mensagemInicio;
	}

	public void setMensagemInicio(String mensagemInicio) {
		this.mensagemInicio = mensagemInicio;
	}

	public String getMensagemFim() {
		return mensagemFim;
	}

	public void setMensagemFim(String mensagemFim) {
		this.mensagemFim = mensagemFim;
	}
}

