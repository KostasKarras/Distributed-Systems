import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class BrokerImpl extends Thread implements Broker{

    public static int ID;
    public static ArrayList<String> brokerHashtags;
    public static byte[] brokerHash;

    public static final int NUM_PUBLISHER_THREADS = 5;
    public static final int NUM_CONSUMER_THREADS = 5;
    public static final int NUM_THREADS = NUM_PUBLISHER_THREADS + NUM_CONSUMER_THREADS;
    public static int current_threads = 0;

    ServerSocket serverSocket;
    Socket connectionSocket = null;

    public static void main(String[] args) {
        new BrokerImpl().start();
        new BrokerImpl().start();
        new BrokerImpl().start();
    }

    static int port = 4321;
    public void run() {
        try {
            serverSocket = new ServerSocket(port++, 6);

            while (true) {
                connectionSocket = serverSocket.accept();

                Thread handler = new Handler(connectionSocket);
                handler.start();

            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    //DONE!
    @Override
    public void init() throws UnknownHostException {

    }

    //DONE!
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

    //DONE!
    @Override
    public Publisher acceptConnection(Publisher publisher) {
        return null;
    }

    //DONE!
    @Override
    public Consumer acceptConnection(Consumer consumer) {
        return null;
    }

    //PENDING!
    @Override
    public void notifyPublisher(String str) {

    }

    //PENDING!
    @Override
    public void notifyBrokersOnChanges() {

    }

    //PENDING!
    @Override
    public void pull(String channel_or_hashtag) {

    }

    //PENDING!
    @Override
    public void filterConsumers() {

    }

    //DONE!
    @Override
    public List<Broker> getBrokers() {
        return brokers;
    }

    //DONE!
    @Override
    public Socket connect() {
        return null;
    }

    //DONE!
    @Override
    public void disconnect() {

    }

    //PENDING!
    @Override
    public void updateNodes() {

    }

    //DONE!
    public int getIPSumValue(String ip) {
        String parts[] = ip.split("\\.");
        int ip_sum = 0;
        for (int part = 0; part<4; part++) {
            ip_sum += Integer.parseInt(parts[part]);
        }
        return ip_sum;
    }
}
