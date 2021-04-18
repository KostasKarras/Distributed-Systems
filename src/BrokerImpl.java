import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Broker broker1 = new BrokerImpl();
        try {
            broker1.initialize(4321);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(int port)  {

        brokerHashtags = new HashMap<>();

        //brokers.add(this);

        Socket connectionSocket = null;

        try {
            serverSocket = new ServerSocket(port);

            String serverSocketAddress = serverSocket.getLocalSocketAddress().toString();
            ID = String.format("Broker_%s", serverSocketAddress);
            int brokerHash = calculateKeys(ID);
            //System.out.println("Hashed:" + brokerHash);

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

    public void connect() {
        //Pass
    }

    public void disconnect() {
        //Pass
    }

    @Override
    public void updateNodes() {

    }

    /**KOSTAS*/
    /** A Thread subclass to handle one client conversation */
    static class Handler extends Thread {

        Socket socket;
        int threadNumber;
        ObjectInputStream objectInputStream;
        ObjectOutputStream objectOutputStream;
        static HashMap<String, ArrayList<byte[]>> VideoFileChunks = new HashMap<String, ArrayList<byte[]>>();
        static ArrayList<byte[]> VideoFile = new ArrayList<byte[]>();
        /**
         * Construct a Handler
         */
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
                int id = (int) objectInputStream.readObject();

                // If-else statements and calling of specific acceptConnection.
                if (id == 1) {
                    handle_push();
                }
                else if (id == 2) {
                    handle_pull();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public void handle_push() {
            try {

                String message;
                message = (String) objectInputStream.readObject();
                if (message.equals("I want to push a new video!"))
                    System.out.println(socket.getInetAddress().getHostAddress() + ">New Publisher connected.");

                objectOutputStream.writeObject("Video is pushing from Publisher to Sever...");
                objectOutputStream.flush();

                byte[] chunk;
                ArrayList<byte[]> chunks = new ArrayList<byte[]>();

                int size = (int) objectInputStream.readObject();

                String hashtags = (String) objectInputStream.readObject();

                for (int i = 0; i < size; i++) {
                    chunk = new byte[4096];
                    chunk = objectInputStream.readAllBytes();
                    chunks.add(chunk);
                }

                VideoFile.addAll(chunks);
                VideoFileChunks.put(hashtags, VideoFile);

                System.out.println(socket.getInetAddress().getHostAddress() + ">Video received Successfully!");
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

        public void handle_pull() {
            try {

                String message;
                message = (String) objectInputStream.readObject();
                if (message.equals("I want to pull a video!"))
                    System.out.println(socket.getInetAddress().getHostAddress() + ">New Consumer connected.");

                objectOutputStream.writeObject(VideoFileChunks.size());
                objectOutputStream.flush();

                String topic = (String) objectInputStream.readObject();

                String videoExists = "false";
                if (VideoFileChunks.containsKey(topic)) {
                    videoExists = "true";
                    objectOutputStream.writeObject("Video has already pulled from Publisher.");
                    objectOutputStream.flush();
                    objectOutputStream.writeObject("Now video is pushing from Server to Consumer...");
                    objectOutputStream.flush();
                } else {
                    objectOutputStream.writeObject("Video doesn't exists.");
                    objectOutputStream.flush();
                    objectOutputStream.writeObject("Try again in the future.");
                    objectOutputStream.flush();
                }
                if (videoExists.equals("true")) {
                    for (Map.Entry<String, ArrayList<byte[]>> item : VideoFileChunks.entrySet()) {
                        byte[] clientToServer = item.getValue().remove(0);
                        objectOutputStream.write(clientToServer);
                        objectOutputStream.flush();
                    }
                }
                objectOutputStream.writeObject(videoExists);
                objectOutputStream.flush();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }finally {
                try {
                    objectInputStream.close();
                    objectOutputStream.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
