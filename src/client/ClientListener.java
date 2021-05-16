package client;

import java.io.IOException;
import java.net.Socket;

import common.Utils;

public class ClientListener implements Runnable {

	private Socket connection;
	private Home home;
	private String connection_info;
	private Chat chat;

	private boolean running;
	private boolean chatOpen;

	public ClientListener(Home home, Socket connection) {
		this.home = home;
		this.connection = connection;
		this.connection_info = null;
		this.chat = null;
		this.chatOpen = false;
		this.running = false;
	}

	public Chat getChat() {
		return chat;
	}

	public void setChat(Chat chat) {
		this.chat = chat;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isChatOpen() {
		return chatOpen;
	}

	public void setChatOpen(boolean chatOpen) {
		this.chatOpen = chatOpen;
	}

	// método de inicialização da Thread.
	@Override
	public void run() {
		running = true;
		String message;
		while (running) {
			message = Utils.receivedMessage(connection);
			if (message == null || message.equals("CHAT_CLOSE")) {
				if (chatOpen) {
					home.getOpened_chats().remove(connection_info);
					home.getConnected_listeners().remove(connection_info);
					chatOpen = false;
					try {
						connection.close();
					} catch (IOException e) {
						System.err.println("[ClientListener:run] -> " + e.getMessage());
					}
					chat.dispose();
				}
				running = false;
			} else {
				String[] fields = message.split(";");
				if (fields.length > 1) {
					if (fields[0].equals("OPEN_CHAT")) {
						@SuppressWarnings("unused")
						String[] splited = fields[1].split(":");
						connection_info = fields[1];
						if (!chatOpen) {
							System.out.println("Abriu o chat...");
							home.getOpened_chats().add(connection_info);
							home.getConnected_listeners().put(connection_info, this);
							chatOpen = true;
							chat = new Chat(home, connection, connection_info, home.getConnection_info().split(":")[0]);
						}
					} else if (fields[0].equals("MESSAGE")) {
						String msg = "";
						for (int i = 0; i < fields.length; i++) {
							msg += fields[i];
							if (i > 1)
								msg += ";";
						}
						chat.append_message(msg);
					}
				}
			}
			System.out.println(">> Mensagem: " + message);
		}
	}

}
