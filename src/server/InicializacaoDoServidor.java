package server;

import client.Login;

public class InicializacaoDoServidor {

	@SuppressWarnings("unused")
	public static void main(String[] args) {

		Login login = new Login();
		Login login2 = new Login();
		Login login3 = new Login();

		Server server = new Server();
	}
}
