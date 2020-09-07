package com.example.tutorial.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

public class BancoDados {

	private static String url;
	private static String usuario;
	private static String senha;
	private static final boolean DEBUG = true;

	public static void dbconfig(PrintStream log) {

		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(new File("/u/atlassian/aplication-data/jira/dbconfig.xml")));
			reader.close();
		} catch (IOException e) {
			e.printStackTrace(log);
		}

	}

	public static Connection conecta(PrintStream log) throws ClassNotFoundException, SQLException {

		url = "jdbc:postgresql://localhost:5432/bd_jira?user=postgres";
		usuario = "postgres";
		senha = "super";
		dbconfig(log);
		return conecta(log, url, usuario, senha);

	}

	public static Connection conecta(PrintStream log, String url, String usuario, String senha)
			throws ClassNotFoundException, SQLException {

		String driver = "org.postgresql.Driver";
		if (DEBUG && (log != null)) {
			log.println("forName: " + driver);
		}
		Class.forName(driver);
		if (DEBUG && (log != null)) {
			log.println("getConnection: " + url);
		}
		Connection conn = DriverManager.getConnection(url, usuario, senha);
		if (DEBUG && (log != null)) {
			log.println("conectado: " + usuario);
		}
		return conn;

	}

	public static void criaTabelas(PrintStream log, Connection conn) throws SQLException {

		if (DEBUG) {
			log.println("verificando tabela: ava_usuarios");
		}
		String sql = "create table if not exists ava_usuarios(usuario char(30) not null primary key"
				+ ", ativo char(1) not null" 
				+ ", constraint ck_ativo check(ativo in ('S', 'N')))";
		Statement stmt = conn.createStatement();
		stmt.execute(sql);

		if (DEBUG) {
			log.println("verificando tabela: ava_status");
		}
		sql = "create table if not exists ava_status(status integer not null primary key"
				+ ", descricao char(30) not null)";
		stmt = conn.createStatement();
		stmt.execute(sql);

		if (DEBUG) {
			log.println("verificando tabela: ava_trabalho");
		}

		sql = "create table if not exists ava_trabalho(usuario char(30) not null" 
				+ ", issue char(20) not null" 
				+ ", inicio timestamp not null"
				+ ", aberto char(1) not null"				
				+ ", status integer not null"
				+ ", fim timestamp" 
				+ ", constraint pk_ava_trabalho primary key(usuario, issue, inicio)"
				+ ", constraint ck_aberto check(aberto in ('S', 'N')))";
		stmt = conn.createStatement();
		stmt.execute(sql);

		try {
			sql = "create index i_status on ava_trabalho(usuario, status)";
			stmt = conn.createStatement();
			stmt.execute(sql);
		} catch (SQLException e) {
		}
		

		//FIXME
		sql = "delete from ava_trabalho";
		stmt = conn.createStatement();
		stmt.execute(sql);

	}

	public static void carregaUsuarios(PrintStream log, Connection conn, String[] usuarios) throws SQLException {

		if (DEBUG) {
			log.println("carregaUsuarios");
		}

		String sql = "delete from ava_usuarios";
		Statement stmt = conn.createStatement();
		stmt.execute(sql);

		sql = "insert into ava_usuarios values(?, 'S')";
		PreparedStatement pStmt = conn.prepareStatement(sql);
		for (String u : usuarios) {
			pStmt.setString(1, u.trim());
			pStmt.executeUpdate();
		}
		pStmt.close();

	}

	public static void carregaStatus(PrintStream log, Connection conn, String[] pares) throws SQLException {

		if (DEBUG) {
			log.println("carregaStatus");
		}

		String sql = "delete from ava_status";
		Statement stmt = conn.createStatement();
		stmt.execute(sql);

		sql = "insert into ava_status values(?, ?)";
		PreparedStatement pStmt = conn.prepareStatement(sql);
		for (String par : pares) {
			String[] partes = par.split("=");
			if (partes.length == 2) {
				pStmt.setInt(1, Integer.parseInt(partes[0]));
				pStmt.setString(2, partes[1]);
				pStmt.executeUpdate();
			}
		}
		pStmt.close();

	}

	public static boolean usuarioAtivo(PrintStream log, Connection conn, String usuario) throws SQLException {

		String sql = String.format("select ativo from ava_usuarios where usuario='%s'", usuario.trim());
		Statement stmt = conn.createStatement();
		ResultSet cursor = stmt.executeQuery(sql);

		boolean ativo = false;
		if (cursor.next()) {
			ativo = cursor.getString("ativo").equals("S");
		}

		cursor.close();
		stmt.close();
		if (DEBUG) {
			log.println("usuarioAtivo: " + usuario + " " + ativo);
		}
		return ativo;
	}

	public static boolean statusRegistra(PrintStream log, Connection conn, int status) throws SQLException {

		String sql = String.format("select descricao from ava_status where status=%d", status);
		Statement stmt = conn.createStatement();
		ResultSet cursor = stmt.executeQuery(sql);

		boolean registra = cursor.next();
		cursor.close();
		stmt.close();
		if (DEBUG) {
			log.println("statusRegistra: " + status + " " + registra);
		}
		return registra;
	}

	public static Trabalho getTrabalho(PrintStream log, Connection conn, String usuario) throws SQLException {

		if (DEBUG) {
			log.println("getTrabalho: " + usuario);
		}

		String sql = String.format(
				"select issue,status,inicio,fim from ava_trabalho " + "where usuario='%s' and aberto='S'",
				usuario.trim());
		Statement stmt = conn.createStatement();
		ResultSet cursor = stmt.executeQuery(sql);

		Trabalho trabalho = null;
		if (cursor.next()) {
			String issue = cursor.getString("issue");
			int status = cursor.getInt("status");
			Timestamp inicio = cursor.getTimestamp("inicio");
			Timestamp fim = cursor.getTimestamp("fim");
			trabalho = new Trabalho(true, issue, status, inicio, fim);
		}

		cursor.close();
		stmt.close();
		if (DEBUG) {
			if (trabalho != null) {
				log.printf("getTrabalho: %s %d %s%n", trabalho.getIssue(), trabalho.getStatus(),
						trabalho.getInicio().toString());
			}
		}
		return trabalho;

	}

	public static void iniciaTrabalho(PrintStream log, Connection conn, String usuario, String issue, int status)
			throws SQLException {

		if (DEBUG) {
			log.println("iniciaTrabalho: " + usuario + " " + issue + " " + status);
		}

		String sql = "insert into ava_trabalho values(?, ?, ?, 'S', ?, null)";
		PreparedStatement pStmt = conn.prepareStatement(sql);
		pStmt.setString(1, usuario.trim());
		pStmt.setString(2, issue.trim());
		pStmt.setTimestamp(3, new java.sql.Timestamp(new Date().getTime()));
		pStmt.setInt(4, status);
		
		pStmt.executeUpdate();
		pStmt.close();

	}

	public static long finalizaTrabalho(PrintStream log, Connection conn, String usuario, Trabalho trabalho)
			throws SQLException {

		if (DEBUG) {
			log.println("finalizaTrabalho: " + usuario + " " + trabalho.getIssue() + " " + trabalho.getInicio());
		}

		String sql = "update ava_trabalho set aberto='N', fim=? where usuario=? and issue=? and inicio=?";
		PreparedStatement pStmt = conn.prepareStatement(sql);
		pStmt.setTimestamp(1, new java.sql.Timestamp(new Date().getTime()));
		pStmt.setString(2, usuario.trim());
		pStmt.setString(3, trabalho.getIssue().trim());
		pStmt.setTimestamp(4, trabalho.getInicio());
		
		pStmt.executeUpdate();
		pStmt.close();
		
		long milliseconds1 = trabalho.getInicio().getTime();
		long milliseconds2 = new Date().getTime();

		long diff = milliseconds2 - milliseconds1;
		long diffMinutes = diff / (60 * 1000);
		return diffMinutes;

	}

}

