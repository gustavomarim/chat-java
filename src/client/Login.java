package client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import common.Utils;
import server.Server;

@SuppressWarnings("serial")
public class Login extends JFrame {
	private JButton jb_login;
	private JLabel jl_user, jl_port, jl_title;
	private JTextField jt_user, jt_port;

	public Login() {
		super();
		initComponents();
		configComponents();
		insertComponents();
		insertActions();
		start();
	}

	// Inicialização de componentes
	private void initComponents() {
		jb_login = new JButton("Entrar");
		jl_user = new JLabel("Apelido", SwingConstants.CENTER); // Centraliza o Label
		jl_port = new JLabel("Porta", SwingConstants.CENTER);
		jl_title = new JLabel();
		jt_user = new JTextField();
		jt_port = new JTextField();
		this.setLocationRelativeTo(null); // Inicia container no centro da tela
	}

	private void configComponents() {
		this.setLayout(null);
		this.setMinimumSize(new Dimension(410, 315));
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.getContentPane().setBackground(Color.WHITE);

		jl_title.setBounds(10, 10, 375, 100);
		ImageIcon icon = new ImageIcon("download.png");
		jl_title.setIcon(new ImageIcon(icon.getImage().getScaledInstance(375, 100, Image.SCALE_SMOOTH)));

		jb_login.setBounds(10, 220, 375, 40);
		jb_login.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		
		jl_user.setBounds(10, 120, 100, 40);
		jl_user.setBorder(BorderFactory.createLineBorder(Color.YELLOW));

		jl_port.setBounds(10, 170, 100, 40);
		jl_port.setBorder(BorderFactory.createLineBorder(Color.GREEN));

		jt_user.setBounds(120, 120, 265, 40);
		jt_user.setBorder(BorderFactory.createLineBorder(Color.GREEN));
		jt_port.setBounds(120, 170, 265, 40);
		jt_port.setBorder(BorderFactory.createLineBorder(Color.YELLOW));

	}

	// Inserindo Componentes no Frame
	private void insertComponents() {
		this.add(jb_login);
		this.add(jl_user);
		this.add(jl_port);
		this.add(jl_title);
		this.add(jt_user);
		this.add(jt_port);
	}

	private void insertActions() {
		jb_login.addActionListener(event -> {
			try {
				String nickname = jt_user.getText();
				jt_user.setText("");
				// Pegando a porta
				int port = Integer.parseInt(jt_port.getText());
				jt_port.setText("");
				// Abrindo nova conexão
				Socket connection = new Socket(Server.HOST, Server.PORT);
				String connection_info = (nickname + ":" + connection.getLocalAddress().getHostAddress() + ":" + port);
				Utils.sendMessage(connection, connection_info);
				if (Utils.receivedMessage(connection).equals("SUCESS")) {
					new Home(connection, connection_info);
					this.dispose();
				} else {
					JOptionPane.showMessageDialog(null,
							"Algum usuário já está conectado com este apelido ou nesse host e porta. Tente outra porta");
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Erro ao conectar. Verifique se o servidor está em execução!");
			}
		});
	}

	private void start() {
		this.pack();
		this.setVisible(true);
	}

}
