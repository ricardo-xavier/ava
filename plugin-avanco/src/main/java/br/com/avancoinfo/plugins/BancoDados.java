package com.example.tutorial.plugins;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

public class BancoDados {

    private static String url;
    private static String usuario;
    private static String senha;
    private static final boolean DEBUG = true;

    public static void dbconfig(PrintStream log) {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("/u/atlassian/aplication-data/jira/dbconfig.xml")));
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
        if (DEBUG) {
            log.println("forName: " + driver);
        }
        Class.forName(driver);
        if (DEBUG) {
            log.println("getConnection: " + url);
        }
        Connection conn =  DriverManager.getConnection(url, usuario, senha);
        if (DEBUG) {
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
            + ", aberto char(1) not null"
            + ", issue char(20) not null"
            + ", status integer not null"
            + ", inicio timestamp not null"
            + ", fim timestamp"
            + ", constraint pk_ava_trabalho primary key(usuario, aberto)"
            + ", constraint ck_aberto check(aberto in ('S', 'N')))";
            //+ ", constraint fk_usuario foreign key(usuario) references ava_usuarios(usuario)"
            //+ ", constraint fk_status foreign key(status) references ava_status(status))"
        stmt = conn.createStatement();
        stmt.execute(sql);
    }

    public static void carregaUsuarios(PrintStream log, Connection conn, String[] usuarios) throws SQLException {

        if (DEBUG) {
            log.println("carregaUsuarios");
        }
        //TODO

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

}
