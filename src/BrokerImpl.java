import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class BrokerImpl implements Broker{

    public static int ID;
    public static ArrayList<String> brokerHashtags;
    public static byte[] brokerHash;

    public static final int NUM_PUBLISHER_THREADS = 5;
    public static final int NUM_CONSUMER_THREADS = 5;
    public static final int NUM_THREADS = NUM_PUBLISHER_THREADS + NUM_CONSUMER_THREADS;
    public static int current_threads = 0;

    public static void main(String[] args) {
       new BrokerImpl();
    }

    public BrokerImpl() {
        try {
            init();
        } catch (UnknownHostException uhe ) {
            uhe.printStackTrace();
        }
    }

    @Override
    public void init() throws UnknownHostException {

        brokerHashtags = new ArrayList<>();

        brokers.add(this);

        ServerSocket serverSocket = null;
        Socket connectionSocket = null;
        String message = null;

        try {
            serverSocket = new ServerSocket(4321);

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        String ip_address = serverSocket.getInetAddress().getLocalHost().getHostAddress();
        int port = serverSocket.getLocalPort();
        ID = getIPSumValue(ip_address) + port;
        brokerHash = calculateKeys(ID);

        while(true) {
            Node x = acceptConnection(serverSocket, connectionSocket);
        }

    }

    @Override
    public byte[] calculateKeys(int id) {

        byte[] digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.putInt(id);
            byte[] idByteArray = bb.array();
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Broker_Operation(socket, current_threads).start();
        current_threads++;

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

    @Override
    public Socket connect() {

        Socket requestSocket = null;
        try {
            requestSocket = new Socket("127.0.0.1", 4321);
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            return requestSocket;
        }
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void updateNodes() {

    }

    public int getIPSumValue(String ip) {
        String parts[] = ip.split("\\.");
        int ip_sum = 0;
        for (int part = 0; part<4; part++) {
            ip_sum += Integer.parseInt(parts[part]);
        }
        return ip_sum;
    }
}
