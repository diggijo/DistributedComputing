import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;

public class ClientGUI extends JFrame {
    private JTextField serverAddressField;
    private JTextField portField;
    private JTextArea messageArea;
    private JTextField commandField;
    private JComboBox<String> commandComboBox;
    private ClientHelper clientHelper;

    public ClientGUI() {
        setTitle("Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel connectionPanel = new JPanel();
        connectionPanel.setLayout(new GridLayout(3, 2));
        connectionPanel.add(new JLabel("Server Address:"));
        serverAddressField = new JTextField();
        connectionPanel.add(serverAddressField);
        connectionPanel.add(new JLabel("Port:"));
        portField = new JTextField();
        connectionPanel.add(portField);
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(new ConnectButtonListener());
        connectionPanel.add(connectButton);
        add(connectionPanel, BorderLayout.NORTH);

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel commandPanel = new JPanel();
        commandPanel.setLayout(new BorderLayout());

        String[] commands = {"logon", "uploadmsg", "downloadmsgs", "logoff"};
        commandComboBox = new JComboBox<>(commands);
        commandPanel.add(commandComboBox, BorderLayout.WEST);

        commandField = new JTextField();
        commandPanel.add(commandField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());
        commandPanel.add(sendButton, BorderLayout.EAST);
        add(commandPanel, BorderLayout.SOUTH);

        clientHelper = new ClientHelper();
    }

    private class ConnectButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String serverAddress = serverAddressField.getText();
            int port = Integer.parseInt(portField.getText());
            try {
                clientHelper.connect(serverAddress, port);
                messageArea.append("Connected to server\n");
                messageArea.append("Enter one of the following commands: \n");
                messageArea.append("1. logon <username> <password>\n");
                messageArea.append("2. uploadmsg <message>\n");
                messageArea.append("3. downloadmsgs <message_id> : message_id is optional\n");
                messageArea.append("4. logoff\n");
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException | CertificateException | KeyStoreException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class SendButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String selectedCommand = (String) commandComboBox.getSelectedItem();
            String parameter = commandField.getText().trim();
            String command = selectedCommand + " " + parameter;

            try {
                clientHelper.sendMessage(command);
                String response = clientHelper.receiveMessage();
                messageArea.append("Server response: " + response + "\n");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            commandField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientGUI clientGUI = new ClientGUI();
            clientGUI.setVisible(true);
        });
    }
}