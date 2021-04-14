import java.util.List;
import java.net.*;
import java.io.*;

public class BrokerImpl implements Broker{

    /** Class Variables */
    private static List<Broker> brokers = null;
    private static List<Consumer> registeredUsers = null;
    private static List<Publisher> registeredPublishers = null;

    /** Main method, to start the servers. */
    public static void main(String[] args) {
        new BrokerImpl().openServer(4321, 4);
    }

    /** Constructor */
    private void openServer(int port, int numberOfThreads) {

        ServerSocket serverSocket;

        try {
            serverSocket = new ServerSocket(port);
        } catch(IOException e) {
            /* Crash the server if IO fails. Something bad has happened. */
            throw new RuntimeException("Could not create ServerSocket ", e);
        }

        // Create a series of threads and start them.
        for (int i=0; i<numberOfThreads; i++) {
            new Handler(serverSocket, i).start();
        }
    }

    /** A Thread subclass to handle one client conversation. */
    static class Handler extends Thread {
        final ServerSocket serverSocket;
        int threadNumber;

        /** Construct a Handler. */
        Handler(ServerSocket s, int i) {
            serverSocket = s;
            threadNumber = i;
            setName("Thread " + threadNumber);
        }

        public void run() {
            // Wait for a connection. Synchronized on the ServerSocket while calling its accept() method.
            while (true) {
                try {
                    System.out.println(getName() + " waiting");

                    Socket clientSocket;
                    // Wait here for the next connection.
                    synchronized (serverSocket) {
                        clientSocket = serverSocket.accept();
                    }
                    System.out.println(getName() + " starting, IP=" + clientSocket.getInetAddress());

                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

                    int id = objectInputStream.readInt();

                    // If-else statements and calling of specific acceptConnection.

                    System.out.println(getName() + " ended");
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println(getName() + ": IO Error on socket " + e);
                    return;
                }
            }
        }
    }

    public List<Broker> getBrokers() {
        return brokers;
    }

    public void calculateKeys() {

    }

    public Publisher acceptConnection(Publisher publisher) {
        return null;
    }

    public Consumer acceptConnection(Consumer consumer) {
        return null;
    }

    public void notifyPublisher(String topic) {

    }

    public void notifyBrokersOnChanges() {

    }

    public void pull(String topic) {

    }

    public void filterConsumers(String id) {

    }

    public void init(int i) {

    }



    public void connect() {

    }

    public void disconnect() {

    }

    public void updateNodes() {

    }
}
