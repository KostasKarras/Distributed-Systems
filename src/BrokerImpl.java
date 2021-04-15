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

    ServerSocket serverSocket;
    Socket connectionSocket = null;

    public static void main(String[] args) {
        new BrokerImpl().openServer();
    }

    void openServer() {
        try {
            serverSocket = new ServerSocket(4321, 6);

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

    @Override
    public void init() throws UnknownHostException {

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
        return null;
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