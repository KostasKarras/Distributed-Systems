import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.List;

public class BrokerImpl implements Broker{

    @Override
    public void init(int i) {

    }

    @Override
    public List<Broker> getBrokers() {
        return null;
    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void updateNodes() {

    }

    @Override
    public void calculateKeys() {

    }

    @Override
    public Publisher acceptConnection(Publisher publisher) {
        return null;
    }

    @Override
    public Consumer acceptConnection(Consumer consumer) {
        return null;
    }

    @Override
    public void notifyPublisher(String str) {

    }

    @Override
    public void notifyBrokersOnChanges() {

    }

    @Override
    public void pull(String channel_or_hashtag) {

    }

    @Override
    public void filterConsumers(String str) {

    }

    public static final int port1 = 4321;
    public static final int port2 = 4421;
    public static final int port3 = 4521;
    public static final int NUM_THREADS = 6;

    public static void main(String[] args){
        BrokerImpl broker1 = new BrokerImpl(port1, NUM_THREADS);
    }

    public BrokerImpl(int port, int numberOfThreads) {
        ServerSocket serverSocket1 = null;
        try {
            serverSocket1 = new ServerSocket(port1);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        for (int i = 0; i < numberOfThreads; i++) {
            new Handler(serverSocket1, i).start();
        }
    }

    class Handler extends Thread {
        ServerSocket serverSock;
        int threadNumber;

        /**
         * Construct a Handler.
         */
        Handler(ServerSocket s, int i) {
            serverSock = s;
            threadNumber = i;
            setName("Thread " + threadNumber);
        }

        public void run() {
            /* Wait for a connection. Synchronized on the ServerSocket
             * while calling its accept() method.
             */
            while (true) {
                try {
                    System.out.println(getName() + " waiting");

                    Socket clientSocket;
                    // Wait here for the next connection.
                    synchronized (serverSock) {
                        clientSocket = serverSock.accept();
                    }
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

                    objectOutputStream.writeObject("Connection Successful!");
                    objectOutputStream.flush();

                    String message = "null";
                    int i = 0;
                    do {
                        byte[] chunk = (byte[]) (objectInputStream.readAllBytes());
                        System.out.println(clientSocket.getInetAddress().getHostAddress() + ">" + chunk);
                    } while (!message.equals("bye"));
                    objectInputStream.close();
                    objectOutputStream.close();
                    clientSocket.close();
                } catch (IOException IOException) {
                    IOException.printStackTrace();
                }
            }
        }
    }
}