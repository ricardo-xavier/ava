package br.com.avancoinfo.integral;

public class Item {
	
	private int nivel;
	private String nome;
	private int occurs;
	private String pic;
	private String valor;
	private boolean array;
	private boolean elementar;
	private int ini;
	private int fim;
	
	@Override
	public String toString() {
		return nivel + " " + nome;
	}
	
	public int getNivel() {
		return nivel;
	}
	public void setNivel(int nivel) {
		this.nivel = nivel;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public int getOccurs() {
		return occurs;
	}
	public void setOccurs(int occurs) {
		this.occurs = occurs;
	}
	public String getPic() {
		return pic;
	}
	public void setPic(String pic) {
		this.pic = pic;
	}
	public String getValor() {
		return valor;
	}
	public void setValor(String valor) {
		this.valor = valor;
	}
	public boolean isArray() {
		return array;
	}
	public void setArray(boolean array) {
		this.array = array;
	}
	public boolean isElementar() {
		return elementar;
	}
	public void setElementar(boolean elementar) {
		this.elementar = elementar;
	}
	public int getIni() {
		return ini;
	}
	public void setIni(int ini) {
		this.ini = ini;
	}
	public int getFim() {
		return fim;
	}
	public void setFim(int fim) {
		this.fim = fim;
	}

}
