package br.com.avancoinfo.integral;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadRedir extends Thread {
	
	private static final int PORTA = 9000;
	
	@Override
	public void run() {
		
		try {
			ServerSocket serverSock = new ServerSocket(PORTA);
			while (true) {
			
				Socket sock = serverSock.accept();
				if (sock == null) {
					break;
				}
				byte[] bytes = new byte[9];
				sock.getInputStream().read(bytes, 0, 9);
				int id = Integer.parseInt(new String(bytes));
				IntegralApi.getSockets().put(id, sock);
				sock.getOutputStream().write(0);
				
			}
			serverSock.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
