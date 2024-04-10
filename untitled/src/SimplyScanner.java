import java.io.IOException;
import java.net.Socket;

public class SimplyScanner {

    public static void main(String[] args) {
        String target = "192.168.56.213";
        int startPort = 1;
        int endPort = 1024;

        for (int port = startPort; port <= endPort; port++) {
            try {
                Socket socket = new Socket(target, port);
                System.out.println("Порт " + port + " открыт");
                socket.close();
            } catch (IOException e) {
                // Порт закрыт
            }
        }
    }

}
