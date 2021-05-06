import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class BrokerImpl implements Broker{

    /** Class Variables */
    private static String ID;
    private static int brokerHash;
    private static int current_threads = 1;

    private static List<Broker> brokers = null;
    private static List<Consumer> registeredUsers = null;
    private static List<Publisher> registeredPublishers = null;

    private static ServerSocket serverSocket;
    private static InetAddress multicastIP;
    private static int multicastPort;

    private static HashMap<String, ArrayList<SocketAddress>> brokerHashtags;
    private static TreeMap<Integer, SocketAddress> brokerHashes;
    private static HashMap<String, SocketAddress> brokerChannelNames;

    private static HashMap<String, ArrayList<SocketAddress>> hashtagSubscriptions;
    private static HashMap<String, ArrayList<SocketAddress>> channelSubscriptions;

    public static void main(String[] args) {

        new BrokerImpl().initialize(4321);
    }


    @Override
    public void initialize(int port)  {

        brokerHashtags = new HashMap<>();
        brokerChannelNames = new HashMap<>();
        brokerHashes = new TreeMap<>();
        hashtagSubscriptions = new HashMap<>();
        channelSubscriptions = new HashMap<>();

        //brokers.add(this);

        Socket connectionSocket = null;

        try {
            serverSocket = new ServerSocket(port, 60, InetAddress.getByName("localhost"));

            multicastIP = InetAddress.getByName("228.5.6.10");
            multicastPort = 5000;

            //CALCULATE BROKERHASH
            String serverSocketAddress = serverSocket.getLocalSocketAddress().toString();
            ID = String.format("Broker_%s", serverSocketAddress);
            brokerHash = calculateKeys(ID);

            //GET LIST FOR BROKERHASHES FROM OTHER BROKERS
            updateNodes();

            //HANDLE MULTICAST
            Multicast_Handler multicast_handler = new Multicast_Handler(multicastIP, multicastPort);
            multicast_handler.start();
            //

            //WAIT FOR CONNECTION WITH BROKER FOR SOME TIME
            serverSocket.setSoTimeout(2000);
            try {
                connectionSocket = serverSocket.accept();
                ObjectInputStream objectInputStream = new ObjectInputStream(connectionSocket.getInputStream());
                int option = (int) objectInputStream.readObject();
                brokerHashes = (TreeMap<Integer, SocketAddress>) objectInputStream.readObject();
            } catch (SocketTimeoutException ste) {
                brokerHashes.put(brokerHash, serverSocket.getLocalSocketAddress());
            }
            serverSocket.setSoTimeout(0);


            while(true) {
                connectionSocket = serverSocket.accept();
                new Handler(connectionSocket, current_threads).start();
                current_threads++;
            }

        } catch(IOException | ClassNotFoundException e) {
            /* Crash the server if IO fails. Something bad has happened. */
            throw new RuntimeException("Could not create ServerSocket ", e);
        } finally {
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
    public HashMap<ChannelKey, String> filterConsumers(HashMap<ChannelKey, String> videoList, String channelName) {
        for (ChannelKey channelKey : videoList.keySet()) {
            if (channelKey.getChannelName() == channelName) {
                videoList.remove(channelKey);
            }
        }
        return videoList;
    }


    @Override
    public TreeMap<Integer, SocketAddress> getBrokerMap() {
        return brokerHashes;
    }

    public void connect() {
        //Pass
    }

    public void disconnect() {
        //Pass
    }

    @Override
    public void updateNodes() throws IOException {

        DatagramSocket socket = new DatagramSocket(4322, InetAddress.getLocalHost());

        //TRANSFORM BROKERHASH AND SOCKET ADDRESS TO STRING, CONCATENATE THEM AND TRANSFORM TO BYTE ARRAY
        String hash = Integer.toString(brokerHash);
        String address = serverSocket.getLocalSocketAddress().toString();
        String hash_address = hash + "," + address;
        byte[] buffer = hash_address.getBytes();

        //MAKE PACKET AND SEND IT
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastIP, multicastPort);
        socket.send(packet);

        //CLOSE SOCKET
        socket.close();

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
                if (option == 0) {  // Send Broker Hashes

                    objectOutputStream.writeObject(brokerHashes);
                    objectOutputStream.flush();
                }

                /** Consumer - User Requests Handle */
                else if (option == 1) {  // Register User
                    /**DIMITRIS*/
                    String channel_name = (String) objectInputStream.readObject();
                    String topic = (String) objectInputStream.readObject();
                    String responseSuccess = "Subscribed to " + topic + " successfully.";
                    String responseFailure = "Attempt to subscribe has failed. Unable to find channel " + topic + ".";

                    /**MAYBE CHANGE THE WAY WE CREATE AN ADDRESS*/
                    SocketAddress user_hear_address = brokerChannelNames.get(channel_name);

                    if (topic.charAt(0) == '#') {
                        if (hashtagSubscriptions.containsKey(topic)) {
                            ArrayList<SocketAddress> value = hashtagSubscriptions.get(topic);
                            value.add(user_hear_address); //proper address
                            hashtagSubscriptions.put(topic, value);
                        } else {
                            ArrayList<SocketAddress> value = new ArrayList<>();
                            value.add(user_hear_address); //proper address
                            hashtagSubscriptions.put(topic, value);
                        }
                        //GIVE SUCCESS MESSAGE
                        objectOutputStream.writeObject(responseSuccess);
                        objectOutputStream.flush();
                    } else {
                        if (channelSubscriptions.containsKey(topic)) {
                            ArrayList<SocketAddress> value = channelSubscriptions.get(topic);
                            value.add(user_hear_address); //proper address
                            channelSubscriptions.put(topic, value);

                            //GIVE SUCCESS MESSAGE
                            objectOutputStream.writeObject(responseSuccess);
                            objectOutputStream.flush();
                        } else {
                            if (brokerChannelNames.containsKey(topic)) {
                                ArrayList<SocketAddress> value = new ArrayList<>();
                                value.add(user_hear_address); //proper address
                                channelSubscriptions.put(topic, value);

                                //GIVE SUCCESS MESSAGE
                                objectOutputStream.writeObject(responseSuccess);
                                objectOutputStream.flush();
                            } else {
                                //GIVE FAILURE MESSAGE
                                objectOutputStream.writeObject(responseFailure);
                                objectOutputStream.flush();
                            }
                        }
                    }

                } else if (option == 2) {  // Get Topic Video List

                    PullOperation pull_operation = new PullOperation();

                    String channel_or_hashtag = (String) objectInputStream.readObject();
                    /**CHANGE*/
                    String channelName = (String) objectInputStream.readObject();
                    /**END CHANGE*/
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

                    /**CHANGE FILTER*/
                    for (ChannelKey channelKey : videoList.keySet()) {
                        if (channelKey.getChannelName() == channelName) {
                            videoList.remove(channelKey);
                        }
                    }
                    //HashMap<ChannelKey, String> videoListUpdated = filterConsumers(videoList, channelName);
                    /**END CHANGE*/

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

                    boolean unique = true;

                    //RECEIVE CHANNEL NAME AND SOCKET ADDRESS FOR CONNECTIONS
                    String channel_name = (String) objectInputStream.readObject();
                    SocketAddress socketAddress = (SocketAddress) objectInputStream.readObject();

                    //CHECK IF CHANNEL NAME IS UNIQUE
                    if (brokerChannelNames.containsKey(channel_name)) {
                        unique = false;
                    }else {
                        brokerChannelNames.put(channel_name, socketAddress);
                    }

                    objectOutputStream.writeObject(unique);
                    objectOutputStream.flush();

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
                } else if (option == 8) { //Notify Brokers for changes
                    String action = (String) objectInputStream.readObject();
                    if (action == "hashtag") {
                        String hashtag = (String) objectInputStream.readObject();
                        ChannelKey channelKey = (ChannelKey) objectInputStream.readObject();
                        String title = (String) objectInputStream.readObject();

                        for (SocketAddress socketAddress : hashtagSubscriptions.get(hashtag)) {
                            new Notifier(socketAddress, channelKey, title, hashtag).start();
                        }

                    } else if (action == "channel") {
                        ChannelKey channelKey = (ChannelKey) objectInputStream.readObject();
                        String title = (String) objectInputStream.readObject();

                        for (SocketAddress socketAddress : channelSubscriptions.get(channelKey.getChannelName())) {
                            new Notifier(socketAddress, channelKey, title, null).start();
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



    /**NEW HANDLER TO SEND NOTIFICATION FOR NEW VIDEOS TO SUBSCRIBED USERS*/
    class Notifier extends Thread {

        ChannelKey channelKey;
        String title;
        String hashtag;
        ObjectInputStream objectInputStream;
        ObjectOutputStream objectOutputStream;

        /** Construct a Handler */
        Notifier(SocketAddress socketAddress, ChannelKey channelKey, String title, String hashtag) {
            this.channelKey = channelKey;
            this.title = title;
            this.hashtag=hashtag;

            Socket connectionSocket = new Socket();
            try {
                connectionSocket.connect(socketAddress);
                objectOutputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
                objectInputStream = new ObjectInputStream(connectionSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            String notificationMessage;

            if (hashtag == null) {
                notificationMessage = "The channel " + channelKey.getChannelName() +
                        " that you are subscribed to has uploaded a new video with title "  + title +
                        " and videoID " + channelKey.getVideoID() + ".";
            } else {
                notificationMessage = "There is a new video in topic " + hashtag +
                        " that you are subscribed, from the channel " + channelKey.getChannelName() + " and title " +
                        title + " with videoID " + channelKey.getVideoID() + ".";
            }

            try {
                objectOutputStream.writeObject(3);
                objectOutputStream.flush();

                objectOutputStream.writeObject(notificationMessage);
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            disconnect();
        }
    }

    /** A Thread subclass to handle broker communication */
    class Multicast_Handler extends Thread {

        private MulticastSocket multicastSocket;
        private DatagramPacket packet_receiver;
        private InetAddress groupAddress ;
        private int multicastPort;

        Multicast_Handler(InetAddress multicastIP, int multicastPort) {

            try {

                //INITIALIZE MULTICAST SOCKET
                this.multicastPort = multicastPort;
                InetAddress brokerIP = InetAddress.getLocalHost();
                SocketAddress multicastSocketAddress = new InetSocketAddress(brokerIP, multicastPort);
                multicastSocket = new MulticastSocket(multicastSocketAddress);

                //JOIN GROUP ADDRESS
                groupAddress = multicastIP;
                multicastSocket.joinGroup(groupAddress);

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

                    //WAIT TO RECEIVE SOME PACKET
                    multicastSocket.receive(packet_receiver);

                    //SPLIT BROKER HASH AND HASH ADDRESS
                    String hash_address = new String(packet_receiver.getData(), packet_receiver.getOffset(),
                                                     packet_receiver.getLength());
                    String[] hash_address_array = hash_address.split(",");
                    int brokerHash = Integer.parseInt(hash_address_array[0]);

                    //SPLIT SOCKET ADDRESS TO IP AND PORT AND CREATE SOCKET ADDRESS OBJECT
                    String[] address = hash_address_array[1].split(":");
                    InetAddress brokerIP = InetAddress.getByName(address[0].substring(10));
                    int brokerPort = Integer.parseInt(address[1]);
                    SocketAddress socketAddress = new InetSocketAddress(brokerIP, brokerPort);

                    //UPDATE BROKER HASHES
                    brokerHashes.put(brokerHash, socketAddress);

                    //SLEEP 1 SECOND TO MAKE SURE THAT SERVER SOCKET HAS STARTED LISTENING
                    TimeUnit.SECONDS.sleep(1);

                    //SEND UPDATED BROKER HASHES TO NEW BROKER
                    Socket socket = new Socket();
                    socket.connect(socketAddress);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

                    objectOutputStream.writeObject(10);
                    objectOutputStream.flush();

                    objectOutputStream.writeObject(brokerHashes);
                    objectOutputStream.flush();

                }
            }
            catch (IOException | InterruptedException ioException) {
                ioException.printStackTrace();
            }
        }

    }
}
