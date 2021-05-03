import java.io.File;
import java.io.FileOutputStream;
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
import java.util.TreeMap;

public class BrokerImpl implements Broker{

    /** Class Variables */
    private static String ID;
    private static int brokerHash;
    private static int current_threads = 1;
    private static List<Broker> brokers = null;
    private static List<Consumer> registeredUsers = null;
    private static List<Publisher> registeredPublishers = null;
    private static HashMap<String, ArrayList<SocketAddress>> hashtagPublisherMap;//<hashtag, ArrayList socketAddress>?
    private static ServerSocket serverSocket;
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

        //brokers.add(this);

        Socket connectionSocket = null;

        try {
            serverSocket = new ServerSocket(port);

            //HANDLE MULTICAST
            new Multicast_Handler().start();
            //

            String serverSocketAddress = serverSocket.getLocalSocketAddress().toString();
            ID = String.format("Broker_%s", serverSocketAddress);
            brokerHash = calculateKeys(ID);
            brokerHashes.put(brokerHash, serverSocket.getLocalSocketAddress());
            //notify other brokers for this ^^^^^!!!!!!!!!!!!

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
        //MULTICAST CHANGES TO OTHER BROKERS
    }

    @Override
    public void pull(String channel_or_hashtag) {

        SocketAddress publisherAddress; //For channel names
        ArrayList<SocketAddress> addresses; // For hashtags (many channels)
        String[] ipPort;
        InetAddress publisher_ip;
        int publisher_port;
        Socket pullSocket;
        ObjectOutputStream objectOutputStream;
        ObjectInputStream objectInputStream;
        HashMap<Integer, String> channelVideoList;

        //Check if this is channel name or hashtag
        try {
            if (channel_or_hashtag.charAt(0) == '#') {
                addresses = brokerHashtags.get(channel_or_hashtag);
                for (SocketAddress address : addresses) {
                    //Generate a thread for each address
                }
            }
            else {
                publisherAddress = brokerChannelNames.get(channel_or_hashtag);

                //Split ip and port from address
                ipPort = publisherAddress.toString().split(":");
                publisher_ip = InetAddress.getByName(ipPort[0].substring(1));
                publisher_port = Integer.parseInt(ipPort[1]);

                //Make connection with client
                pullSocket = new Socket(publisher_ip, publisher_port);
                objectInputStream = new ObjectInputStream(pullSocket.getInputStream());
                objectOutputStream = new ObjectOutputStream(pullSocket.getOutputStream());

                //Give option code
                objectOutputStream.writeObject(1);
                objectOutputStream.flush();

                //Give operation
                objectOutputStream.writeObject("CHANNEL");
                objectOutputStream.flush();

                //Store channel videos
                channelVideoList = (HashMap<Integer, String>) objectInputStream.readObject();
                System.out.println(channelVideoList);

            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void filterConsumers() {

    }


    @Override
    public TreeMap<Integer, SocketAddress> getBrokerMap() {
        return null;
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
                int option = (int) objectInputStream.readObject();
                // If-else statements and calling of specific acceptConnection.

                /** Node Requests Handle */
                if (option == 8) {  // Get Brokers
                    objectOutputStream.writeObject(brokerHashes);
                }

                /** Consumer - User Requests Handle */
                else if (option == 1) {  // User Subscription
                    /**DIMITRIS*/
                    String topic = (String) objectInputStream.readObject();

                    //MAJOR FIX!!!!!!!!!!!!!!

                    InetAddress ip_address = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress();
                    SocketAddress user_hear_address = new InetSocketAddress(ip_address, 4900);

                    if (Character.compare(topic.charAt(0),'#') == 0) {
                        if (hashtagSubscriptions.containsKey(topic)) {
                            ArrayList<SocketAddress> value = hashtagSubscriptions.get(topic);
                            value.add(user_hear_address); //proper address
                            hashtagSubscriptions.put(topic, value);
                        } else {
                            ArrayList<SocketAddress> value = new ArrayList<>();
                            value.add(user_hear_address); //proper address
                            hashtagSubscriptions.put(topic, value);
                        }
                    } else {
                        if (channelSubscriptions.containsKey(topic)) {
                            ArrayList<SocketAddress> value = channelSubscriptions.get(topic);
                            value.add(user_hear_address); //proper address
                            channelSubscriptions.put(topic, value);
                        } else {
                            ArrayList<SocketAddress> value = new ArrayList<>();
                            value.add(user_hear_address); //proper address
                            channelSubscriptions.put(topic, value);
                        }
                    }

                } else if (option == 2) {  // Get Topic Video List

                } else if (option == 3) {  // Play Data

                }

                /** Publisher Requests Handle */
                else if (option == 4) {  // Hash Topic? OXI

                } else if (option == 5) {  // Push?

                } else if (option == 6) {  // Notify Failure?

                } else if (option == 7) {  // Notify Brokers for Hashtags

                    /**DIMITRIS*/
                    String action = (String) objectInputStream.readObject();
                    String hashtag = (String) objectInputStream.readObject();

//                    String channel = (String) objectInputStream.readObject(); // MAYBE NEEDED TO STREAM VIDEO TO SUBSCRIBERS
//                    int videoID =  (int) objectInputStream.readObject(); // MAYBE NEEDED TO STREAM VIDEO TO SUBSCRIBERS

                    if (action.equals("ADD")) {
                        if (hashtagPublisherMap.get(hashtag) == null) {
                            ArrayList<SocketAddress> value = new ArrayList<>();
                            value.add(this.socket.getRemoteSocketAddress());//??
                            hashtagPublisherMap.put(hashtag, value);
                        } else {
                            ArrayList<SocketAddress> value = hashtagPublisherMap.get(hashtag);
                            value.add(this.socket.getRemoteSocketAddress());//??
                            hashtagPublisherMap.put(hashtag, value);
                        }
                        /**CHANGE*/
                        //Notify subscribed Users.
//                        ArrayList<SocketAddress> subscribedInChannel = channelSubscriptions.get(channel);
                        ArrayList<SocketAddress> subscribedInHashtag = hashtagSubscriptions.get(hashtag);

//                        if (subscribedInChannel != null) {
//                            for (SocketAddress user_address : subscribedInChannel) {
//                                new Notifier(user_address, channel, videoID).start();
//                            }
//                        }

                        if (subscribedInHashtag != null) {
                            for (SocketAddress user_address : subscribedInHashtag) {
                                return;
                            }
                        }
                        /**END CHANGE*/
                    } else if (action.equals("REMOVE")) {
                        if (hashtagPublisherMap.get(hashtag).size() > 1) {
                            ArrayList<SocketAddress> value = hashtagPublisherMap.get(hashtag);
                            value.remove(this.socket.getRemoteSocketAddress());//??
                            hashtagPublisherMap.put(hashtag, value);
                        } else {
                            hashtagPublisherMap.remove(hashtag);
                        }
                    }

                    //channel & hashtag subscriptions stream videos.

                    notifyBrokersOnChanges();

                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /**NEW HANDLER TO SEND NOTIFICATION FOR NEW VIDEOS TO SUBSCRIBED USERS*/
        class Notifier extends Thread {

            Socket socket;
            ObjectInputStream objectInputStream;
            ObjectOutputStream objectOutputStream;
            String channel;
            int videoID;
            String hashtag;

            /** Construct a Handler */
            Notifier(SocketAddress socketAddress, String channel, int videoID, String hashtag) {
                this.channel = channel;
                this.videoID = videoID;
                this.hashtag = hashtag;
                Socket connectionSocket = new Socket();
                try {
                    connectionSocket.connect(socketAddress);
                    objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectInputStream = new ObjectInputStream(socket.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void run() {

                String message;

                if (hashtag == null) {
                    message = "A new video has been uploaded from the channel: " + channel + "that you follow.";
                } else {
                    message = "A new video has been uploaded from the hashtag: " + hashtag + "that you follow.";
                }

                String action_message = "Do you want to play this video? (y/n)";

                String response = null;

                try {
                    objectOutputStream.writeObject(message);
                    objectOutputStream.writeObject(action_message);
                    response = (String) objectInputStream.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (response == "y") {
                    ChannelKey channelKey = new ChannelKey(channel, videoID);
                    playData(channelKey);
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

    /** A Thread subclass to handle broker communication */
    class Multicast_Handler extends Thread {

        public MulticastSocket multicastSocket;
        public DatagramPacket packet_receiver;

        Multicast_Handler() {

            try {

                //INITIALIZE MULTICAST SOCKET
                int multicastPort = 5000;
                InetAddress brokerIP = InetAddress.getByName("192.168.2.54");
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