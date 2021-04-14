import java.util.List;
import java.net.*;
import java.io.*;

public class BrokerImpl implements Broker {

    /** Class Variables */
    private static String ID;
    private static byte[] brokerHash;
    private static int current_threads = 1;
    private static List<Broker> brokers = null;
    private static List<Consumer> registeredUsers = null;
    private static List<Publisher> registeredPublishers = null;

    /** Main method, to start the servers */
    public static void main(String[] args) {
        new BrokerImpl().initialize(4321);
    }

    /** Initializer Method */
    public void initialize(int port) {

        brokers.add(this);

        ServerSocket serverSocket;
        Socket connectionSocket;

        try {
            serverSocket = new ServerSocket(port);
        } catch(IOException e) {
            /* Crash the server if IO fails. Something bad has happened. */
            throw new RuntimeException("Could not create ServerSocket ", e);
        }

        String ip_address = serverSocket.getInetAddress().toString();
        ID = String.format("Broker_%s:%d", ip_address, port);
        brokerHash = calculateKeys(ID);

        while (true) {
            try {
                connectionSocket = serverSocket.accept();
                new Handler(connectionSocket, current_threads).start();
                current_threads++;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** A Thread subclass to handle one client conversation */
    static class Handler extends Thread {
        final Socket socket;
        int threadNumber;
        ObjectInputStream objectInputStream;
        ObjectOutputStream objectOutputStream;

        /** Construct a Handler */
        Handler(Socket s, int current_thread) {
            socket = s;
            threadNumber = current_thread;
            setName("Thread " + threadNumber);
        }

        public void run() {
            while (true) {
                try {
                    objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectInputStream = new ObjectInputStream(socket.getInputStream());

                    int id = objectInputStream.readInt();

                    // If-else statements and calling of specific acceptConnection.

                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public byte[] calculateKeys(String ID) {
        return null;
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

    public List<Broker> getBrokers() {
        return brokers;
    }

    public Socket connect() {
        Socket requestSocket = null;

        try {
            requestSocket = new Socket("127.0.0.1", 4321);
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return requestSocket;
    }

    public void disconnect(Socket s) {
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    public void updateNodes() {

    }
}
