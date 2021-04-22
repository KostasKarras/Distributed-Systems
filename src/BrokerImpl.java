import javax.xml.crypto.Data;
import java.io.*;
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
    private static HashMap<String, ArrayList<SocketAddress>> brokerHashtags;

    private static ServerSocket serverSocket;

    //CHANGE
    private static InetAddress userMulticastIP;
        //We make brokerHashes treemap because we need hashes sorted in
        //hashTopic in AppNode
    static TreeMap<Integer, SocketAddress> brokerHashes;
    private static HashMap<String, SocketAddress> brokerChannelNames;
    //

    public static void main(String[] args) {

        //TEST
        try {
            ServerSocket s = new ServerSocket(4321);
            String[] a = s.getLocalSocketAddress().toString().split(":");
            System.out.println(Integer.parseInt(a[1]) + 5);
            s.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        //
        new BrokerImpl().initialize(4321);
    }

    @Override
    public void initialize(int port)  {

        //CHANGE
        brokerHashtags = new HashMap<>();
        brokerChannelNames = new HashMap<>();
        brokerHashes = new TreeMap<>();
        //CHANGE

        Socket connectionSocket = null;

        try {
            serverSocket = new ServerSocket(port);

            //CHANGE
            userMulticastIP = InetAddress.getByName("228.5.6.8");
            //

            //HANDLE MULTICAST
            new Multicast_Handler().start();

            String serverSocketAddress = serverSocket.getLocalSocketAddress().toString();
            ID = String.format("Broker_%s", serverSocketAddress);
            brokerHash = calculateKeys(ID);
            brokerHashes.put(brokerHash, serverSocket.getLocalSocketAddress());

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
                String[] ipPort = publisherAddress.toString().split(":");
                InetAddress publisher_ip = InetAddress.getByName(ipPort[0]);
                int publisher_port = Integer.parseInt(ipPort[1]);

                //Make connection with client
                Socket pullSocket = new Socket(publisher_ip, publisher_port);
                ObjectInputStream objectInputStream = new ObjectInputStream(pullSocket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(pullSocket.getOutputStream());

                //Give option code
                objectOutputStream.writeObject(1);
                objectOutputStream.flush();

                //Give operation
                objectOutputStream.writeObject("CHANNEL");
                objectOutputStream.flush();

                //Store channel videos

                //return
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
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

                    int option = (int) objectInputStream.readObject();;
                    // If-else statements and calling of specific acceptConnection.

                    /** Node Requests Handle */
                    if (option == 0) {  // Get Brokers

                    }

                    /** Consumer - User Requests Handle */
                    else if (option == 1) {  // Register User

                    } else if (option == 2) {  // Get Topic Video List*

                    } else if (option == 3) {  // Play Data*

                    }

                    /** Publisher Requests Handle */
                    else if (option == 4) {  // Hash Topic?

                    } else if (option == 5) {  // Push?

                    } else if (option == 6) {  // Notify Failure?

                    } else if (option == 7) {  // Notify Brokers for Hashtags

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

                    if (message == "break") {
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
