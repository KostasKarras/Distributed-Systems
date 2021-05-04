import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;

public class BrokerImpl implements Broker{

    /** Class Variables */
    private static String ID;
    private static int brokerHash;
    private static int current_threads = 1;

    private static List<Broker> brokers = null;
    private static List<Consumer> registeredUsers = null;
    private static List<Publisher> registeredPublishers = null;

    private static ServerSocket serverSocket;

    private static HashMap<String, ArrayList<SocketAddress>> brokerHashtags;
    private static TreeMap<Integer, SocketAddress> brokerHashes;
    private static HashMap<String, SocketAddress> brokerChannelNames;

    private static InetAddress userMulticastIP;

    public static void main(String[] args) {

        new BrokerImpl().initialize(4321);
    }


    @Override
    public void initialize(int port)  {

        brokerHashtags = new HashMap<>();
        brokerChannelNames = new HashMap<>();
        brokerHashes = new TreeMap<>();

        //brokers.add(this);

        Socket connectionSocket = null;

        try {
            serverSocket = new ServerSocket(port, 60, InetAddress.getByName("localhost"));

            userMulticastIP = InetAddress.getByName("228.5.6.8");

            //HANDLE MULTICAST
            new Multicast_Handler().start();
            //

            String serverSocketAddress = serverSocket.getLocalSocketAddress().toString();
            ID = String.format("Broker_%s", serverSocketAddress);
            brokerHash = calculateKeys(ID);
            brokerHashes.put(brokerHash, serverSocket.getLocalSocketAddress());
            //notify other brokers for this ^^^^^


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
        connect();

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

    /** A Thread subclass to handle one client conversation */
    class Handler extends Thread {

        Socket socket;
        int threadNumber;
        ObjectInputStream objectInputStream;
        ObjectOutputStream objectOutputStream;

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
                int option = (int) objectInputStream.readObject();
                // If-else statements and calling of specific acceptConnection.
                /** Node Requests Handle */
                if (option == 0) {  // Get Brokers

                }

                /** Consumer - User Requests Handle */
                else if (option == 1) {  // Register User

                } else if (option == 2) {  // Get Topic Video List

                    PullOperation pull_operation = new PullOperation();

                    String channel_or_hashtag = (String) objectInputStream.readObject();
                    HashMap<ChannelKey, String> videoList = null;

                    if (channel_or_hashtag.charAt(0) == '#') {
                        ArrayList<SocketAddress> addresses = brokerHashtags.get(channel_or_hashtag);
                        if (addresses != null)
                            videoList = pull_operation.pullHashtags(channel_or_hashtag, addresses);
                    } else {
                        SocketAddress publisherAddress = brokerChannelNames.get(channel_or_hashtag);
                        if (publisherAddress != null)
                            videoList = pull_operation.pullChannel(publisherAddress);
                    }
                    objectOutputStream.writeObject(videoList);


                } else if (option == 3) {// Play Data

                    try {
                        PullOperation pull_operation = new PullOperation();

                        //RECEIVE CHANNEL KEY AND EXTRACT SOCKET ADDRESS OF PUBLISHER
                        ChannelKey key = (ChannelKey) objectInputStream.readObject();
                        SocketAddress publisherAddress = brokerChannelNames.get(key.getChannelName());

                        //PULL VIDEO FROM PUBLISHER
                        ArrayList<byte[]> video_chunks = pull_operation.pullVideo(key, publisherAddress);

                        //SEND VIDEO CHUNKS
                        objectOutputStream.writeObject(video_chunks.size());
                        objectOutputStream.flush();

                        while (!video_chunks.isEmpty()) {
                            byte[] clientToServer = video_chunks.remove(0);
                            objectOutputStream.write(clientToServer);
                            objectOutputStream.flush();
                        }

                    } catch (IOException | ClassNotFoundException ioException) {
                        ioException.printStackTrace();
                    } catch (NoSuchElementException nsee) {
                        objectOutputStream.writeObject("This channel doesn't exist");
                        objectOutputStream.flush();
                    }
                } else if (option == 4) { //FIRST CONNECTION
                    //SEND BROKER HASHES
                    objectOutputStream.writeObject(brokerHashes);
                    objectOutputStream.flush();

                    //RECEIVE CHANNEL NAME
                    String channel_name = (String) objectInputStream.readObject();
                    SocketAddress socketAddress = (SocketAddress) objectInputStream.readObject();
                    brokerChannelNames.put(channel_name, socketAddress);

                    /** Publisher Requests Handle */

                } else if (option == 5) {  // Push?

                } else if (option == 6) {  // Notify Failure?

                } else if (option == 7) {  // Notify Brokers for Hashtags

                    String hashtag = (String) objectInputStream.readObject();
                    String action = (String) objectInputStream.readObject();
                    SocketAddress socketAddress = (SocketAddress) objectInputStream.readObject();

                    if (action.equals("ADD")) {
                        if (brokerHashtags.get(hashtag) == null) {
                            ArrayList<SocketAddress> value = new ArrayList<>();
                            value.add(socketAddress);
                            brokerHashtags.put(hashtag, value);
                        } else {
                            ArrayList<SocketAddress> value = brokerHashtags.get(hashtag);
                            value.add(socketAddress);
                            brokerHashtags.put(hashtag, value);
                        }
                    } else if (action.equals("REMOVE")) {
                        if (brokerHashtags.get(hashtag).size() > 1) {
                            ArrayList<SocketAddress> value = brokerHashtags.get(hashtag);
                            value.remove(socketAddress);
                            brokerHashtags.put(hashtag, value);
                        } else {
                            brokerHashtags.remove(hashtag);
                        }
                    }
                }
                try {
                    objectInputStream.close();
                    objectOutputStream.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /** A Thread subclass to handle broker communication */
    class Multicast_Handler extends Thread {

        public MulticastSocket multicastSocket;
        public DatagramPacket packet_receiver;

        Multicast_Handler() {

            try {

                //INITIALIZE MULTICAST SOCKET
                int multicastPort = 5000;
                InetAddress brokerIP = InetAddress.getByName("192.168.1.203");
                SocketAddress multicastSocketAddress = new InetSocketAddress(brokerIP, multicastPort);
                multicastSocket = new MulticastSocket(multicastSocketAddress);

                //JOIN GROUP ADDRESS
                InetAddress group_address = InetAddress.getByName("228.5.6.10");
                multicastSocket.joinGroup(group_address);

                //INITIALIZE DATAGRAM PACKET
                byte buf[] = new byte[1000];
                packet_receiver = new DatagramPacket(buf, buf.length);
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        @Override
        public void run() {

            //RECEIVE PACKET
            try {

                while (true) {
                    multicastSocket.receive(packet_receiver);
                    String message = new String(packet_receiver.getData(), packet_receiver.getOffset(), packet_receiver.getLength());
                    System.out.println(message);

                    if (message.equals("break")) {
                        break;
                    }

                }
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
