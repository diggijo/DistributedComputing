import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int DEFAULT_PORT = 12345;
    private static final List<String> messages = new ArrayList<>();
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    public static void main(String[] args) {
        int port;

        try (BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print("Enter port number: ");
            String portStr = userInput.readLine();
            port = Integer.parseInt(portStr);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Invalid port number. Using default port: 12345");
            port = DEFAULT_PORT;
        }

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            String keystorePath = System.getProperty("javax.net.ssl.keyStore");
            String keystorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);

            System.out.println("Server started. Listening on port " + port);
            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                Thread thread = new Thread(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
            }
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | CertificateException | UnrecoverableKeyException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        boolean isLoggedIn = false;

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            String[] tokens = inputLine.split(" ");
            String command = tokens[0];
            String inMessage = "";

            for (int i = 1; i < tokens.length; i++)
            {
                inMessage += tokens[i];
                if (i < tokens.length - 1) {
                    inMessage += " ";
                }
            }

            System.out.println("Message Received. Command: " + command + ", Message: " + inMessage);

            if (!isLoggedIn && !command.equals("logon")) {
                out.println("401 Unauthorized. Please log in first.");
                continue;
            }

            switch (command) {
                case "logon":
                    if (tokens.length == 3)
                    {
                        if(!tokens[1].equals(USERNAME) || !tokens[2].equals(PASSWORD))
                        {
                            out.println("401 Unauthorized Access. Please enter a valid username and password.");
                            System.out.println("Response : Access Denied");
                        }

                        else
                        {
                            out.println("200 OK Logon successful.");
                            System.out.println("Response : Access Granted");
                            isLoggedIn = true;
                        }
                    }
                    else
                    {
                        out.println("400 Logon unsuccessful. This message requires two parameters <username><password>.");
                        System.out.println("Response : Access Denied");
                    }
                    break;
                case "uploadmsg":
                    if (tokens.length < 2) {
                        out.println("400 No Message Entered. Please enter a message.");
                        System.out.println("Response : No Message Entered");
                    }
                    else
                    {
                        StringBuilder messageBuilder = new StringBuilder();
                        for (int i = 1; i < tokens.length; i++) {
                            messageBuilder.append(tokens[i]);
                            if (i < tokens.length - 1) {
                                messageBuilder.append(" ");
                            }
                        }
                        String message = messageBuilder.toString();
                        messages.add(message);
                        out.println("200 OK Message uploaded successfully.");
                        System.out.println("Response : Message: " + message + " uploaded successfully");
                    }
                    break;
                case "downloadmsgs":
                    if (tokens.length == 1) {
                        if (messages.isEmpty()) {
                            out.println("404 No messages found");
                            System.out.println("Response : No Messages");
                        } else {
                            out.println("200 OK Messages: " + (messages));
                            System.out.println("Response : You have " + messages.size() + " message(s)");
                        }
                    } else if (tokens.length == 2) {
                        try {
                            int userInput = (Integer.parseInt(tokens[1]));
                            int index =  userInput - 1;

                            if (index >= 0 && index < messages.size()) {
                                out.println("200 OK " + messages.get(index));
                                System.out.println("Response : Message " + userInput + ": " + messages.get(index));
                            } else {
                                out.println("404 Message not found with message_id " + userInput);
                                System.out.println("No message with message_id " + userInput);
                            }
                        } catch (NumberFormatException e) {
                            out.println("400 Invalid Number");
                            System.out.println("Response: Invalid Number");
                        }
                    } else {
                        out.println("400 This function takes maximum one parameter");
                        System.out.println("Response: Too many parameters");
                    }
                    break;
                case "logoff":
                    out.println("200 OK Logoff successful");
                    System.out.println("Response: Logoff successful");
                    clientSocket.close();
                    return;
                default:
                    out.println("400 Bad Request Unknown command");
                    System.out.println("Response: Logoff unsuccessful");
            }
        }
    }
}
