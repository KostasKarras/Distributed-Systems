import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.net.*;
import java.io.*;

public class BrokerImpl implements Broker {

    /** Class Variables */
    private static String ID;
    private static int brokerHash;
    private static int current_threads = 1;
    private static List<Broker> brokers;
    private static List<Consumer> registeredUsers;
    private static List<Publisher> registeredPublishers;

    /** Main method, to start the servers */
    public static void main(String[] args) {
        new BrokerImpl().initialize(4321);
    }

    /** Initializer Method */
    public void initialize(int port) {

        ServerSocket serverSocket;
        Socket connectionSocket;

        try {
            serverSocket = new ServerSocket(port);
        } catch(IOException e) {
            /* Crash the server if IO fails. Something bad has happened. */
            throw new RuntimeException("Could not create ServerSocket ", e);
        }

        String ip_address = serverSocket.getInetAddress().toString();
        String serverSocketAddress = serverSocket.getLocalSocketAddress().toString();
        ID = String.format("Broker_%s", serverSocketAddress);
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

                    int id = objectInputStream.readInt();

                    // If-else statements and calling of specific acceptConnection.
                    if (id == 1) {
                        handle_push();
                    }

                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void handle_push() {
            try {

                String message;
                message = (String) objectInputStream.readObject();
                if(message.equals("I want to push a new video!"))
                    System.out.println(socket.getInetAddress().getHostAddress() + ">New Client connected.");

                objectOutputStream.writeObject("Video is pushed...");
                objectOutputStream.flush();

                byte[] chunk;
                ArrayList<byte[]> chunks = new ArrayList<byte[]>();

                int size = (int) objectInputStream.readObject();
                System.out.println("Size of the Arraylist is: " + size);

                for (int i = 0;i < size;i++){
                    chunk = new byte[4096];
                    chunk = objectInputStream.readAllBytes();
                    chunks.add(chunk);
                    System.out.println(this.socket.getInetAddress().getHostAddress() + ">" + chunk.toString());
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

    public int calculateKeys(String ID) {
        int digest = 0;
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] bb = sha256.digest(ID.getBytes(StandardCharsets.UTF_8));
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