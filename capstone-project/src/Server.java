import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Server {
    public void run() {
        try {
            // Create a new ServerSocket object listening on port 8080
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("Server is listening on port 8080");

            // Accept an incoming client connection
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected");

            // Get the output stream of the socket and wrap it in a PrintWriter
            OutputStream output = clientSocket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            // Send a response to the client
            writer.println("Hello, client!");

            // Close the socket
            clientSocket.close();
            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}