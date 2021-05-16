package client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import common.Utils;

@SuppressWarnings("serial")
public class Chat extends JFrame {

	private JLabel jl_title;
	private JEditorPane messages;
	private JTextField jt_message;
	private JButton jb_message;
	private JPanel panel;
	private JScrollPane scroll;

	private Home home;
	private Socket connection;
	private ArrayList<String> message_list; // Array comportará as mensagens
	private String connection_info;

	// connection_info => informação de quem estou conectado;
	// title => Título do usuário do chat
	public Chat(Home home, Socket connection, String connection_info, String title) {
		super(title);
		this.home = home;
		this.connection = connection;
		this.connection_info = connection_info;
		initComponents();
		configComponents();
		insertComponents();
		insertActions();
		start();

	}

	private void initComponents() {
		message_list = new ArrayList<String>();
		jl_title = new JLabel("< " + this.getTitle() + " > está conversando com < "+connection_info.split(":")[0] + " >", SwingConstants.CENTER);
		messages = new JEditorPane();
		scroll = new JScrollPane(messages);
		jt_message = new JTextField();
		jb_message = new JButton("Enviar");
		panel = new JPanel(new BorderLayout());

	}

	private void configComponents() {
		this.setMinimumSize(new Dimension(480, 720));
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		messages.setContentType("text/html");
		messages.setEditable(false); // torna mensagens não editáveis
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jb_message.setSize(100, 40);

	}

	// Inserindo Componentes no Frame
	private void insertComponents() {
		// Setando as coordernadas dos componentes
		this.add(jl_title, BorderLayout.NORTH);
		this.add(scroll, BorderLayout.CENTER);
		this.add(panel, BorderLayout.SOUTH);
		panel.add(jt_message, BorderLayout.CENTER);
		panel.add(jb_message, BorderLayout.EAST);

	}

	// Inserção de comando de teclado para envio de mensagens
	private void insertActions() {
		jb_message.addActionListener(event -> send());
		jt_message.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyReleased(KeyEvent e) {}

			// add evento de clique do Enter p/ enviar mensagem
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					send();
				}
			}
		});
		
		// Sequência de ações de janela para o encerramento do chat
		this.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {}
			
			@Override
			public void windowIconified(WindowEvent e) {}
			
			@Override
			public void windowDeiconified(WindowEvent e) {}
			
			@Override
			public void windowDeactivated(WindowEvent e) {}
			
			@Override
			public void windowClosing(WindowEvent e) {
				Utils.sendMessage(connection, "CHAT_CLOSE");
				home.getOpened_chats().remove(connection_info); // removida a lista de chats
				home.getConnected_listeners().get(connection_info).setChatOpen(false); // chat fechado
				home.getConnected_listeners().get(connection_info).setRunning(false); // listenner removido
				home.getConnected_listeners().remove(connection_info); // remove da lista de escutas
			}
			
			@Override
			public void windowClosed(WindowEvent e) {}
			
			@Override
			public void windowActivated(WindowEvent e) {}
			
		});
	}

	// Atualiza a caixa de mensagem sempre que houver um novo texto
	public void append_message(String received) {
		message_list.add(received);
		String message = "";
		for (String str : message_list) {
			message += str;
		}
		messages.setText(message);
	}

	// Envio de mensagem feito pelo ladoda interface.
	private void send() {
		if (jt_message.getText().length() > 0) {
			DateFormat df = new SimpleDateFormat("hh:mm:ss");
			Utils.sendMessage(connection, "MESSAGE;" + "<b>[" + df.format(new Date()) +
					"] " + this.getTitle() + ": </b><i> " + jt_message.getText() + "</i><br>");
			append_message("<b>[" + df.format(new Date()) + "] Eu: </b></i> "
					+ jt_message.getText() + "</i><br>");
			jt_message.setText("");
		}
	}

	private void start() {
		this.pack();
		this.setVisible(true);
	}

}
