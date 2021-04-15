import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BrokerImpl implements Broker{

    /** Class Variables */
    private static String ID;
    private static byte[] brokerHash;
    private static int current_threads = 1;
    private static List<Broker> brokers = null;
    private static List<Consumer> registeredUsers = null;
    private static List<Publisher> registeredPublishers = null;
    private static HashMap<String, String> brokerHashtags;

    public static void main(String[] args) {
        try {
            new BrokerImpl().initialize(4321);
        } catch (UnknownHostException uhe ) {
            uhe.printStackTrace();
        }
    }

    @Override
    public void initialize(int port) throws UnknownHostException {

        brokerHashtags = new HashMap<>();

        //brokers.add(this);

        ServerSocket serverSocket = null;
        Socket connectionSocket = null;

        try {
            serverSocket = new ServerSocket(4321);

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        ID = serverSocket.getLocalSocketAddress().toString();
        brokerHash = calculateKeys(ID);

        while(true) {
            Node x = acceptConnection(serverSocket, connectionSocket);
        }

    }

    @Override
    public byte[] calculateKeys(String id) {

        byte[] digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] idByteArray = id.getBytes();
            md.update(idByteArray);
            digest = md.digest();
        }
        catch (NoSuchAlgorithmException nsae) {
            nsae.printStackTrace();
        }
        finally {
            return digest;
        }

    }

    public Node acceptConnection(ServerSocket serverSocket, Socket socket) {
        try {
            socket = serverSocket.accept();
            new Handler(socket, current_threads).start();
            current_threads++;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        return null;
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
    public void filterConsumers() {

    }


    @Override
    public List<Broker> getBrokers() {
        return brokers;
    }

    public Socket connect() {
        Socket requestSocket = null;

        try {
            requestSocket = new Socket(InetAddress.getByName("127.0.0.1"), 4321);
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
    @Override
    public void updateNodes() {

    }

    /** A Thread subclass to handle one client conversation */
    class Handler extends Thread {
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

}
