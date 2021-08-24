/*
 * Leonardo Vona
 * 545042
 */
package wordquizzle.client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

/*
 * Gestore della GUI del client
 */
public class ClientGUI {
	// login
	private JLabel usernameLabel;
	private JLabel passwordLabel;
	protected JTextField usernameField;
	protected JPasswordField passwordField;
	private JButton loginButton;
	private JButton signInButton;
	private JPanel loginPanel;

	// sign in
	private JLabel signUsernameLabel;
	private JLabel signPasswordLabel;
	private JLabel signRepeatPasswordLabel;
	private JButton signGoBackButton;
	private JButton createAccountButton;
	protected JTextField signUsernameField;
	protected JTextField signPasswordField;
	protected JTextField signRepeatedPasswordField;
	private JPanel signInPanel;

	// main page
	private JButton challengeButton;
	private JButton addFriendButton;
	private JButton logoutButton;
	protected JLabel scoreLabel;
	private JPanel mainPagePanel;
	private JScrollPane scrollPane;
	protected JTable rankingTable;

	// add friend
	private JLabel friendLabel;
	protected JTextField friendUsernameField;
	private JButton addButton;
	private JPanel addFriendPanel;
	private JButton goBackFriendsButton;

	// request challenge
	protected JComboBox<String> friendsComboBox;
	private JPanel requestChallengePanel;
	private JButton requestButton;
	private JButton goBackChallengeButton;

	// challenge
	protected JLabel wordLabel;
	private JLabel translationLabel;
	protected JTextField translationField;
	private JButton sendButton;
	private JButton quitButton;
	private JPanel challengePanel;

	private JPanel cardPanel;
	private JFrame window;
	private CardLayout layout; // cardLayout per poter cambiare vari Pane all'interno dello stesso Panel

	private ActionListener listener; // listener degli eventi

	public ClientGUI(ActionListener listener) {
		this.listener = listener;
		window = new JFrame("Word Quizzle");
		layout = new CardLayout();
		cardPanel = new JPanel(layout);

		// crea i vari pannelli
		loginPanel();

		signInPanel();

		mainPagePanel();

		requestChallengePanel();

		addFriendPanel();

		challengePanel();

		window.setContentPane(cardPanel);
		layout.show(cardPanel, "Login");
		window.setSize(800, 600);
		window.setLocation(100, 100);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
	}

	private void loginPanel() {
		usernameLabel = new JLabel("Username");
		passwordLabel = new JLabel("Password");
		loginButton = new JButton("Login");
		createAccountButton = new JButton("Create Account");
		loginButton.setEnabled(true);
		loginButton.addActionListener(listener);
		createAccountButton.setEnabled(true);
		createAccountButton.addActionListener(listener);
		usernameField = new JTextField("", 10);
		passwordField = new JPasswordField("", 10);
		loginPanel = new JPanel();
		loginPanel.add(usernameLabel);
		loginPanel.add(usernameField);
		loginPanel.add(passwordLabel);
		loginPanel.add(passwordField);
		loginPanel.add(loginButton);
		loginPanel.add(createAccountButton);
		cardPanel.add(loginPanel, "Login");
	}

	private void signInPanel() {

		signUsernameLabel = new JLabel("Username");
		signPasswordLabel = new JLabel("Password");
		signRepeatPasswordLabel = new JLabel("Repeat password");

		signInButton = new JButton("Sign In");
		signInButton.setEnabled(true);
		signInButton.addActionListener(listener);

		signGoBackButton = new JButton("Cancel");
		signGoBackButton.setEnabled(true);
		signGoBackButton.addActionListener(listener);

		signUsernameField = new JTextField("", 10);
		signPasswordField = new JTextField("", 10);
		signRepeatedPasswordField = new JTextField("", 10);
		signInPanel = new JPanel();
		signInPanel.add(signUsernameLabel);
		signInPanel.add(signUsernameField);
		signInPanel.add(signPasswordLabel);
		signInPanel.add(signPasswordField);
		signInPanel.add(signRepeatPasswordLabel);
		signInPanel.add(signRepeatedPasswordField);
		signInPanel.add(signInButton);
		signInPanel.add(signGoBackButton);
		cardPanel.add(signInPanel, "Sign In");
	}

	private void mainPagePanel() {
		challengeButton = new JButton("Challenge");
		addFriendButton = new JButton("Add Friend");
		logoutButton = new JButton("Logout");

		challengeButton.addActionListener(listener);
		addFriendButton.addActionListener(listener);
		logoutButton.addActionListener(listener);

		challengeButton.setEnabled(true);
		addFriendButton.setEnabled(true);
		logoutButton.setEnabled(true);

		scoreLabel = new JLabel("Your score is: ");

		scrollPane = new JScrollPane();
		rankingTable = new JTable();

		scrollPane.setViewportView(rankingTable);

		mainPagePanel = new JPanel();
		mainPagePanel.add(challengeButton);
		mainPagePanel.add(addFriendButton);
		mainPagePanel.add(logoutButton);
		mainPagePanel.add(scoreLabel);
		mainPagePanel.add(scrollPane);

		cardPanel.add(mainPagePanel, "Main Page");
	}

	private void addFriendPanel() {
		friendUsernameField = new JTextField("", 10);

		friendLabel = new JLabel("Friend username");

		addButton = new JButton("Add");
		addButton.setEnabled(true);
		addButton.addActionListener(listener);

		goBackFriendsButton = new JButton("Go Back");
		goBackFriendsButton.addActionListener(listener);

		addFriendPanel = new JPanel();
		addFriendPanel.add(friendLabel);
		addFriendPanel.add(friendUsernameField);
		addFriendPanel.add(addButton);
		addFriendPanel.add(goBackFriendsButton);
		cardPanel.add(addFriendPanel, "Add Friend");
	}

	private void requestChallengePanel() {

		friendsComboBox = new JComboBox<>();

		goBackChallengeButton = new JButton("Go Back");
		goBackChallengeButton.addActionListener(listener);

		requestButton = new JButton("Request challenge");
		requestButton.addActionListener(listener);

		requestChallengePanel = new JPanel();
		requestChallengePanel.add(friendsComboBox);
		requestChallengePanel.add(requestButton);
		requestChallengePanel.add(goBackChallengeButton);

		cardPanel.add(requestChallengePanel, "Challenge Request");
	}

	private void challengePanel() {
		wordLabel = new JLabel();
		translationLabel = new JLabel("Translation: ");
		translationField = new JTextField("", 10);
		sendButton = new JButton("Send");
		quitButton = new JButton("Exit");
		challengePanel = new JPanel();

		sendButton.addActionListener(listener);
		quitButton.addActionListener(listener);

		challengePanel = new JPanel(null);

		Dimension size = wordLabel.getPreferredSize();
		wordLabel.setBounds(350, 60, 200, 50);

		size = translationLabel.getPreferredSize();
		translationLabel.setBounds(200, 200, size.width, size.height);

		size = translationField.getPreferredSize();
		translationField.setBounds(350, 200, size.width, size.height);

		size = sendButton.getPreferredSize();
		sendButton.setBounds(500, 200, size.width, size.height);

		size = quitButton.getPreferredSize();
		quitButton.setBounds(370, 500, size.width, size.height);

		challengePanel.add(wordLabel);
		challengePanel.add(translationLabel);
		challengePanel.add(translationField);
		challengePanel.add(sendButton);
		challengePanel.add(quitButton);

		cardPanel.add(challengePanel, "Challenge");
	}

	// cambia Pane
	protected void changePane(String pane) {
		layout.show(cardPanel, pane);
	}

	// mostra una dialog di informazione
	protected void showMessage(String message) {
		JOptionPane.showMessageDialog(window, message);
	}

	// mostra una dialog di errore
	protected void showError(String message) {
		JOptionPane.showMessageDialog(window, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	// mostra la dialog di attesa per la conferma di accettazione della sfida
	protected JDialog loadWaitDialog() {
		final JDialog loading = new JDialog(window);
		JPanel p1 = new JPanel(new BorderLayout());
		p1.add(new JLabel("Please wait..."), BorderLayout.CENTER);
		loading.setUndecorated(true);
		loading.getContentPane().add(p1);
		loading.pack();
		loading.setLocationRelativeTo(window);
		loading.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		loading.setModal(true);

		return loading;
	}

	// mostra una dialog di conferma e ritorna la risposta
	protected int showConfirm(String message) {
		return JOptionPane.showConfirmDialog(window, message);
	}

}
