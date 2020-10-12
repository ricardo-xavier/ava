package br.com.avancoinfo.integral;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadRedir extends Thread {
	
	private static int porta = 8081;
	
	@Override
	public void run() {
		
		try {
			ServerSocket serverSock = new ServerSocket(porta);
			while (true) {
			
				Socket sock = serverSock.accept();
				if (sock == null) {
					break;
				}
				String s = Comunicacao.recebeMensagem(sock.getInputStream());
				int id = Integer.parseInt(s);
				IntegralApi.getSockets().put(id, sock);
				
			}
			serverSock.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public static int getPorta() {
		return porta;
	}

	public static void setPorta(int porta) {
		ThreadRedir.porta = porta;
	}

}
