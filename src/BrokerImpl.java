import java.io.File;
import java.io.FileOutputStream;
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
import java.net.UnknownHostException;
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
    private static ServerSocket serverSocket;
    private static HashMap<String, ArrayList<SocketAddress>> brokerHashtags;
    private static TreeMap<Integer, SocketAddress> brokerHashes;
    private static HashMap<String, SocketAddress> brokerChannelNames;

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
                SocketAddress address;
                while(!addresses.isEmpty()){
                    //Generate a thread for each address
                    address = addresses.remove(0);
                    ipPort = address.toString().split(":");
                    publisher_ip = InetAddress.getByName(ipPort[0].substring(1));
                    publisher_port = Integer.parseInt(ipPort[1]);
                    new MultipleConnections(publisher_ip, publisher_port, channel_or_hashtag).start();
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
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

    }

    class MultipleConnections extends Thread{
        Socket requestSocket;
        ObjectInputStream objectInputStream;
        ObjectOutputStream objectOutputStream;
        String hashtag;
        ArrayList<VideoFile> videoFiles;
        String channelName;

        /** Construct a Handler */
        MultipleConnections(InetAddress publisherIp, int publisherPort, String hashtag) {
            this.hashtag = hashtag;
            try {
                requestSocket = new Socket(publisherIp, publisherPort);
                objectOutputStream = new ObjectOutputStream(requestSocket.getOutputStream());
                objectInputStream = new ObjectInputStream(requestSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try{
                //Give option code
                objectOutputStream.writeObject(1);
                objectOutputStream.flush();

                //Give operation
                objectOutputStream.writeObject("HASHTAG");
                objectOutputStream.flush();

                //Give hashtag
                objectOutputStream.writeObject(hashtag);
                objectOutputStream.flush();

                //Store channel videos
                videoFiles = (ArrayList<VideoFile>) objectInputStream.readObject();
                channelName = (String) objectInputStream.readObject();
                for (VideoFile video : videoFiles){
                    System.out.printf("Video Id: %d \t Channel Name: %s \t Video Name: ",video.getVideoID(), channelName, video.getVideoName());
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
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
                if (option == 0) {  // Get Brokers

                }

                /** Consumer - User Requests Handle */
                else if (option == 1) {  // Register User

                } else if (option == 2) {  // Get Topic Video List

                } else if (option == 3) {  // Play Data

                }

                /** Publisher Requests Handle */
                else if (option == 4) {  // Hash Topic?

                } else if (option == 5) {  // Push?

                } else if (option == 6) {  // Notify Failure?

                } else if (option == 7) {  // Notify Brokers for Hashtags |||||CHANNELS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    String hashtag = (String) objectInputStream.readObject();
                    String message = (String) objectInputStream.readObject();
                    SocketAddress notificationSocket = (SocketAddress) objectInputStream.readObject();
                    if (message.equals("add")) {
                        if (brokerHashtags.containsKey(hashtag)) {
                            if (brokerHashtags.get(hashtag).contains(notificationSocket))
                                System.out.println("Publisher is already in the List.");
                            else
                                brokerHashtags.get(hashtag).add(notificationSocket);
                        }
                        else {
                            ArrayList<SocketAddress> Sockets = new ArrayList<>();
                            Sockets.add(notificationSocket);
                            brokerHashtags.put(hashtag, Sockets);
                        }
                    } else {
                        if (brokerHashtags.containsKey(hashtag)){
                            if (brokerHashtags.get(hashtag).size() > 1)
                                brokerHashtags.get(hashtag).remove(notificationSocket);
                            else {
                                brokerHashtags.remove(hashtag);
                            }
                        } else {
                            System.out.println("No Publisher is responsible for hashtag: " + hashtag);
                        }
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
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
                    File nf = new File("C:/Users/Kostas/Desktop/test.mp4");
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
                InetAddress brokerIP = InetAddress.getByName("192.168.2.4");
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
