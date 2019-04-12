/**
 * We hereby acknowledge that the work handed in is our own original work. If We
 * have quoted from any other source this information has been correctly referenced.
 * We also declare that We have read the Namibia University of Science and Technology
 * Policies on Academic Honesty and Integrity as indicated in our course outline and
 * the NUST general information and regulations - Yearbook 2018
 *
 * @authors:
 * <Kuizikee Raphael Maendo> <217101461>
 * <Diina Kalimba><218095597>
 *<Nkomba Waltraud M><218102062>
 *<Werner Eliaser><218062958>
 *<Nekundi Eunike M N><217070590>
 */
public class ServerNode<AnyType> {
private AnyType data = null;
private ServerNode<AnyType> server = null;

public  ServerNode(AnyType data){
    this.data=data;
}

}//end class ServerNode
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

/**
 *
 * @author Werner
 */
public class ServerNode {

    private static ServerSocket serverSocket = null;// The server socket.
    private static Socket clientSocket = null; // The client socket.
    private static final int maxClientsCount = 10; // This chat server can accept up to maxClientsCount clients' connections.
    private final clientThread[] threads = new clientThread[maxClientsCount];

    public void Broker(String[] message){
        int portNumber = 2222;
        if (message.length < 1) {
            System.out.println("Now using port number : " +portNumber);
        } else {
            portNumber = Integer.parseInt(message[0]);
        }
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println(e);
        }

        //Create a client socket for each connection and pass it to a new client thread.
        while (true) {
            try {
                clientSocket = serverSocket.accept();
               int i=0;
                for (i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new clientThread(clientSocket, threads)).start();
                        break;
                    }
                }
                if (i == maxClientsCount) {
                    try (PrintStream os = new PrintStream(clientSocket.getOutputStream())) {
                        os.println("Server too busy. Try later.");
                    }
                    clientSocket.close();
                }
            } 
            catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public class clientThread extends Thread {

        private DataInputStream is = null;
        private PrintStream os = null;
        private Socket clientSocket = null;
        private final clientThread[] threads;
        private int maxClientsCount;

        public clientThread(Socket clientSocket, clientThread[] threads) {
            this.clientSocket = clientSocket;
            this.threads = threads;
            maxClientsCount = threads.length;
        }

        @Override
        public void run() {
            int maxClientsCount = this.maxClientsCount;
            clientThread[] threads = this.threads;

            try {
                // Create input and output streams for this client.

                is = new DataInputStream(clientSocket.getInputStream());
                os = new PrintStream(clientSocket.getOutputStream());
                os.println("Enter your name.");
                String name = is.readLine().trim();
                os.println("Hello " + name + " Welcome to our chat room.\nTo leave type 'Bye' in a new line");
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {
                        threads[i].os.println("*** A new user " +name+ "entered the chat room !!! ***");
                    }
                }
                while (true) {
                    String line = is.readLine();
                    if (line.startsWith("Bye")) {
                        break;
                    }
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null) {
                            threads[i].os.println(name + " :" + line);
                        }
                    }
                }
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {
                        threads[i].os.println("*** The user " +name+ "is leaving");
                    }
                }
                os.println("*** Bye " + name + " ***");

                /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
                 */
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == this) {
                        threads[i] = null;
                    }
                }

                is.close();   //Closing  the streams.
                os.close();
                clientSocket.close();
            }
            catch (IOException e) {
                System.out.println(e);
            }
        }
    }

}
