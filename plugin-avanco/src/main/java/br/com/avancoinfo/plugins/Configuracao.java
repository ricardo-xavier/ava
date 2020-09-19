package com.example.tutorial.plugins;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Configuracao {

    private String listaUsuarios;
    private String listaEventosInicio;
    private String listaStatusInicio;
    private String listaEventosFim;
    private String mensagemInicio;
    private String mensagemFim;
    private String feriados;
    private List<String> turnos;
    
    private String iniManha;
    private String fimManha;
    private String iniTarde;
    private String fimTarde;
    
	public Configuracao() {
	    listaUsuarios = "teste.plugin";
	    listaEventosInicio = "11";
	    listaStatusInicio = "3";
	    listaEventosFim = "12";
	    mensagemInicio = "Registro de trabalho iniciado pelo plugin avanço";
	    mensagemFim = "Trabalho registrado pelo plugin avanço";
	    feriados = "";
	    turnos = new ArrayList<String>();
	}
	
	public boolean carregaTurno(String usuario) {
		
        int p1 = listaUsuarios.indexOf(":" + usuario + "(");
        if (p1 < 0) {
        	return false;
        }
        int p2 = listaUsuarios.indexOf("(", p1);
        if (p2 < 0) {
        	return false;
        }        
        int p3 = listaUsuarios.indexOf(")", p2);
        if (p3 < 0) {
        	return false;
        }        
        String turno = "turno" + listaUsuarios.substring(p2+1, p3) + "=";
        for (String t : turnos) {
        	if (t.startsWith(turno)) {
        		String[] partes = t.substring(turno.length()).split(",");
        		if (partes.length != 4) {
        			return false;
        		}
        		iniManha = partes[0];
        		fimManha = partes[1];
        		iniTarde = partes[2];
        		fimTarde = partes[3];
        	}
        }
		return false;
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
                listaUsuarios = ":" + linha.substring(p+1).trim() + ":";
                
            } else if (linha.startsWith("eventos_inicio=")) {
                listaEventosInicio = ":" + linha.substring(p+1).trim() + ":";
                
            } else if (linha.startsWith("status_inicio=")) {
                listaStatusInicio = ":" + linha.substring(p+1).trim() + ":";
                
            } else if (linha.startsWith("eventos_fim=")) {
                listaEventosFim = ":" + linha.substring(p+1).trim() + ":";
                
            } else if (linha.startsWith("mensagem_inicio=")) {
                mensagemInicio = linha.substring(p+1).trim();
                
            } else if (linha.startsWith("mensagem_fim=")) {
                mensagemFim = linha.substring(p+1).trim();
                
            } else if (linha.startsWith("turno")) {
                String turno = linha.trim();
                turnos.add(turno);
                
            } else if (linha.startsWith("feriados=")) {
                feriados = linha.substring(p+1).trim();
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
				+ "\n  mensagemFim       =" + mensagemFim
				+ "\n  feriados          =" + feriados
				+ "\n  turnos            =" + turnos.size();
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

	public String getFeriados() {
		return feriados;
	}

	public void setFeriados(String feriados) {
		this.feriados = feriados;
	}

	public List<String> getTurnos() {
		return turnos;
	}

	public void setTurnos(List<String> turnos) {
		this.turnos = turnos;
	}

	public String getIniManha() {
		return iniManha;
	}

	public void setIniManha(String iniManha) {
		this.iniManha = iniManha;
	}

	public String getFimManha() {
		return fimManha;
	}

	public void setFimManha(String fimManha) {
		this.fimManha = fimManha;
	}

	public String getIniTarde() {
		return iniTarde;
	}

	public void setIniTarde(String iniTarde) {
		this.iniTarde = iniTarde;
	}

	public String getFimTarde() {
		return fimTarde;
	}

	public void setFimTarde(String fimTarde) {
		this.fimTarde = fimTarde;
	}
}

