import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;

public class ClientHelper {
    private SSLSocket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void connect(String serverAddress, int port) throws IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, CertificateException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        String trustStorePath = System.getProperty("javax.net.ssl.trustStore");
        String trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(new FileInputStream(trustStorePath), trustStorePassword.toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        socket = (SSLSocket) sslSocketFactory.createSocket(serverAddress, port);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void sendMessage(String message) throws IOException {
        out.println(message);
    }

    public String receiveMessage() throws IOException {
        return in.readLine();
    }

    public void disconnect() throws IOException {
        if (socket != null) {
            socket.close();
        }
    }
}