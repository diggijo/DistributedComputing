import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class Client {
    private static String serverAddress;
    private static int port;

    public static void main(String[] args) {
        try (BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print("Enter server address: ");
            serverAddress = userInput.readLine();

            System.out.print("Enter port number: ");
            String portStr = userInput.readLine();
            port = Integer.parseInt(portStr);

            try {
                ClientHelper clientHelper = new ClientHelper();
                clientHelper.connect(serverAddress, port);

                System.out.println("Connected to server");

                while (true) {
                    System.out.println("Enter one of the following commands: ");
                    System.out.println("1. logon <username> <password>");
                    System.out.println("2. uploadmsg <message>");
                    System.out.println("3. downloadmsgs <message_id> : message_id is optional");
                    System.out.println("4. logoff");
                    String command = userInput.readLine();

                    if (command.equals("exit")) {
                        clientHelper.sendMessage("logoff");
                        System.out.println("Logging off...");
                        break;
                    }

                    clientHelper.sendMessage(command);
                    String response = clientHelper.receiveMessage();
                    System.out.println("Server response: " + response);
                }

                clientHelper.disconnect();
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException | CertificateException e) {
                e.printStackTrace();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}