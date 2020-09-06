package com.example.tutorial.plugins;

import java.sql.Timestamp;

public class Trabalho {
	
	private boolean aberto;
	private String issue;
	private int status;
	private Timestamp inicio;
	private Timestamp fim;
	
	public Trabalho(boolean aberto, String issue, int status, Timestamp inicio, Timestamp fim) {
		super();
		this.aberto = aberto;
		this.issue = issue;
		this.status = status;
		this.inicio = inicio;
		this.fim = fim;
	}
	public boolean isAberto() {
		return aberto;
	}
	public void setAberto(boolean aberto) {
		this.aberto = aberto;
	}
	public String getIssue() {
		return issue;
	}
	public void setIssue(String issue) {
		this.issue = issue;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Timestamp getInicio() {
		return inicio;
	}
	public void setInicio(Timestamp inicio) {
		this.inicio = inicio;
	}
	public Timestamp getFim() {
		return fim;
	}
	public void setFim(Timestamp fim) {
		this.fim = fim;
	}

}

