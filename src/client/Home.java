package client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import common.Utils;

@SuppressWarnings("serial")
public class Home extends JFrame {

	private ArrayList<String> opened_chats; // Array p/ comportar os chats abertos
	private Map<String, ClientListener> connected_listeners; // ref. p/ todos os clientsListeners dos chats abertos
	private ArrayList<String> connected_users;
	private String connection_info;
	private Socket connection;
	private ServerSocket server;
	private boolean running;

	private JLabel jl_title;
	private JButton jb_get_connected;
	private JButton jb_start_talk;
	private JList<Object> jlist;
	private JScrollPane scroll;

	public Home(Socket connection, String connection_info) {
		super("Chat - Home");
		this.connection = connection;
		this.connection_info = connection_info;
		initComponents();
		configComponents();
		insertComponents();
		insertActions();
		start();
	}

	private void initComponents() {
		running = false;
		server = null;
		connected_listeners = new HashMap<String, ClientListener>();
		opened_chats = new ArrayList<String>();
		connected_users = new ArrayList<String>();
		jl_title = new JLabel("< Usuário : " + connection_info.split(":")[0] + " >", SwingConstants.CENTER);// faz aseparação das strings utilizando 
		//o caractere > ; < , começando pela primeira letra e centralizando o label
		jb_get_connected = new JButton("Atualizar Contatos");
		jb_start_talk = new JButton("Abrir Conversa");
		jlist = new JList<>();
		scroll = new JScrollPane(jlist); // Scroll recebe a lista de usuários
	}

	private void configComponents() {
		this.setLayout(null);
		this.setMinimumSize(new Dimension(610, 500));
		this.setResizable(false); // torna a janela não redimensionável
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.getContentPane().setBackground(Color.WHITE);
		this.setLocationRelativeTo(null); // Inicia o Container no centro da tela

		// Largura e cor da borda
		jl_title.setBounds(10, 10, 370, 40);
		jl_title.setBorder(BorderFactory.createLineBorder(Color.GREEN));

		jb_get_connected.setBounds(400, 10, 180, 40);
		jb_get_connected.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		jb_get_connected.setFocusable(false); // desabilita o foco do botï¿½o

		jb_start_talk.setBounds(10, 400, 573, 40);
		jb_start_talk.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		jb_start_talk.setFocusable(false);

		jlist.setBorder(BorderFactory.createTitledBorder("Usuários Online"));
		// Seta a abertura de uma conversa por vez
		jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scroll.setBounds(10, 60, 575, 335);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // seta a barra lateral apenas
																						// quando necessário

		scroll.setBorder(null);

	}

	// Inserindo Componentes no Frame
	private void insertComponents() {
		this.add(jl_title);
		this.add(jb_get_connected);
		this.add(scroll);
		this.add(jb_start_talk);
	}

	private void insertActions() {
		// Ação p/ remover o erro que aparece ao fechar conexão
		this.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				running = false;
				Utils.sendMessage(connection, "QUIT");
				System.out.println("> Conexão encerrada.");
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}
		});

		jb_get_connected.addActionListener(event -> getConnectedUsers());
		jb_start_talk.addActionListener(event -> openChat());

	}

	private void start() {
		this.pack();
		this.setVisible(true);
		startServer(this, Integer.parseInt(connection_info.split(":")[2]));
	}

	private void getConnectedUsers() {
		Utils.sendMessage(connection, "GET_CONNECTED_USERS");
		String response = Utils.receivedMessage(connection);
		jlist.removeAll();
		connected_users.clear();
		for (String info : response.split(";")) {
			if (!info.equals(connection_info)) {
				connected_users.add(info);
			}
		}
		jlist.setListData(connected_users.toArray());
	}

	public ArrayList<String> getOpened_chats() {
		return opened_chats;
	}

	public void setOpened_chats(ArrayList<String> opened_chats) {
		this.opened_chats = opened_chats;
	}

	public Map<String, ClientListener> getConnected_listeners() {
		return connected_listeners;
	}

	public void setConnected_listeners(Map<String, ClientListener> connected_listeners) {
		this.connected_listeners = connected_listeners;
	}

	public ArrayList<String> getConnected_users() {
		return connected_users;
	}

	public String getConnection_info() {
		return connection_info;
	}

	// Caso não haja nenhum chat aberto, será instanciado um novo chat e suas infos
	// (ip, usuário) serão inseridas em um array
	private void openChat() {
		int index = jlist.getSelectedIndex();
		if (index != -1) {
			String connection_info = jlist.getSelectedValue().toString();
			String[] splited = connection_info.split(":");
			if (!opened_chats.contains(connection_info)) {
				try {
					Socket connection = new Socket(splited[1], Integer.parseInt(splited[2]));
					Utils.sendMessage(connection, "OPEN_CHAT;" + this.connection_info);
					ClientListener cl = new ClientListener(this, connection);
					cl.setChat(new Chat(this, connection, connection_info, this.connection_info.split(":")[0]));
					cl.setChatOpen(true);
					connected_listeners.put(connection_info, cl);
					opened_chats.add(connection_info);
					new Thread(cl).start();
				} catch (IOException e) {
					System.err.println("[Home:openChat] -> " + e.getMessage());
				}
			}
		}

	}

	// Rodando o chat em uma Thread. Dessa forma múltiplos chats podem conversar
	// simultâneamente
	private void startServer(Home home, int port) {
		new Thread() {
			@Override
			public void run() {
				running = true;
				try {
					server = new ServerSocket(port);
					System.out.println("Servidor cliente iniciado na porta: " + port + "...");
					while (running) {
						Socket connection = server.accept();
						ClientListener cl = new ClientListener(home, connection);
						new Thread(cl).start();
					}
				} catch (IOException e) {
					System.err.println("[Home:startServer] -> " + e.getMessage());
				}
			}
		}.start();
	}

}
