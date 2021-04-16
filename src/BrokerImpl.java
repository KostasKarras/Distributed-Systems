import javax.xml.crypto.Data;
import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BrokerImpl implements Broker{

    /** Class Variables */
    private static String ID;
    private static int brokerHash;
    private static int current_threads = 1;
    private static List<Broker> brokers = null;
    private static List<Consumer> registeredUsers = null;
    private static List<Publisher> registeredPublishers = null;
    private static HashMap<String, String> brokerHashtags;
    private static ServerSocket serverSocket;

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

        Socket connectionSocket = null;

        try {
            serverSocket = new ServerSocket(port);

            ID = serverSocket.getLocalSocketAddress().toString();
            int brokerHash = calculateKeys(ID);

            while(true) {
                connectionSocket = serverSocket.accept();
                new Handler(connectionSocket, current_threads).start();
                current_threads++;
            }
        } catch(IOException e) {
            /* Crash the server if IO fails. Something bad has happened. */
            throw new RuntimeException("Could not create ServerSocket ", e);
        }
        finally {
            try {
                serverSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    @Override
    public int calculateKeys(String id) {

        int digest = 0;
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] bb = sha256.digest(id.getBytes(StandardCharsets.UTF_8));
            BigInteger bigInteger = new BigInteger(1, bb);
            digest = bigInteger.intValue();

            return digest;
        }
        catch (NoSuchAlgorithmException nsae) {
            nsae.printStackTrace();
        }
        finally {
            return digest;
        }
    }

    public void acceptConnection(ServerSocket serverSocket, Socket socket) {
        try {
            socket = serverSocket.accept();
            new Handler(socket, current_threads).start();
            current_threads++;
        }
        catch (IOException e) {
            e.printStackTrace();
        }

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

    public void disconnect() {}

    @Override
    public void updateNodes() {

    }

    /** A Thread subclass to handle one client conversation */
    class Handler extends Thread {

        Socket socket;
        int threadNumber;
        ObjectInputStream objectInputStream;
        ObjectOutputStream objectOutputStream;
        DataInputStream dataInputStream;

        /** Construct a Handler */
        Handler(Socket s, int current_thread) {
            socket = s;
            threadNumber = current_thread;
            setName("Thread " + threadNumber);
            try {
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectInputStream = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public void run() {

                try {

                    while(true) {

                        int id = (int) objectInputStream.readObject();

                        // If-else statements and calling of specific acceptConnection.
                        if (id == 0) {
                            System.out.println("I got 0!");
                            objectInputStream.close();
                            objectOutputStream.close();
                            socket.close();
                            break;
                        }
                        else if (id == 1) {
                            handle_push();
                        }
                        else {
                            break;
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
        }

        public void handle_push() {
            try {
                byte[] chunk;
                ArrayList<byte[]> chunks = new ArrayList<byte[]>();

                int size = (int) objectInputStream.readObject();
                System.out.println("Size of the Arraylist is: " + size);

                for (int i = 0;i < size;i++){
                    chunk = new byte[4096];
                    chunk = objectInputStream.readAllBytes();
                    chunks.add(chunk);
                    System.out.println(this.socket.getInetAddress().getHostAddress() + ">" + chunk);
                }

                System.out.println("My Arraylist size: " + chunks.size());

                try {
                    File nf = new File("C:/Users/miked/Desktop/test.mp4");
                    for (byte[] ar : chunks) {
                        FileOutputStream fw = new FileOutputStream(nf, true);
                        try {
                            fw.write(ar);
                        } finally {
                            fw.close();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    objectInputStream.close();
                    objectOutputStream.close();
                    socket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

    }

}
