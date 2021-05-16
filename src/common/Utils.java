package common;

import java.io.IOException;

import java.io.*;
import java.net.Socket;

public class Utils {

	public static boolean sendMessage(Socket connection, String message) {

		try {
			// Saída de mensagem
			ObjectOutputStream output = new ObjectOutputStream(connection.getOutputStream());
			output.flush();
			// escreve a mensagem do objeto
			output.writeObject(message);
		} catch (IOException e) {
			System.err.println("[ERROR:sendMessage] -> " + e.getMessage());
		}

		return false;
	}

	public static String receivedMessage(Socket connection) {
		String response = null;

		try {
			// Lendo conexão
			ObjectInputStream input = new ObjectInputStream(connection.getInputStream());
			// lendo o objeto recebido pela mensagem
			response = (String) input.readObject();
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("[ERROR:receivedMessage] -> " + e.getMessage());
		}
		return response;
	}

}
