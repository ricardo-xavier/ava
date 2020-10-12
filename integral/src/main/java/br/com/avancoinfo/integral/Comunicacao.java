package br.com.avancoinfo.integral;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Comunicacao {

	public static String recebeMensagem(InputStream inputStream) throws IOException {
		byte[] bytes = readNBytes(inputStream, 9);
		int n = Integer.parseInt(new String(bytes));
		bytes = readNBytes(inputStream, n);
		//System.err.println("recebe " + n + " " + new String(bytes));
		return new String(bytes);
	}
	
	private static byte[] readNBytes(InputStream inputStream, int n) throws IOException {
		byte[] bytes = new byte[n];
		int pos = 0;
		while (n > 0) {
			int b = inputStream.read(bytes, pos, n);
			n -= b;
			pos += b;
		}
		return bytes;
	}

	public static void enviaMensagem(OutputStream outputStream, String mensagem) throws IOException {
		int n = mensagem.length();
		String s = String.format("%09d", n);
		outputStream.write(s.getBytes(), 0, 9);
		//System.err.println("envia " + 9 + " " + s);
		outputStream.write(mensagem.getBytes(), 0, n);
		//System.err.println("envia " + n + " " + mensagem);
	}	
	
}
