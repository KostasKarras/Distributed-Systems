import java.io.IOException;
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

public class BrokerImpl extends Thread implements Broker{

    public HashMap<String, Integer> brokersHashCodes = new HashMap<String, Integer>();

    ServerSocket serverSocket;
    Socket connectionSocket = null;

    public static void main(String[] args) {
        BrokerImpl broker1 = new BrokerImpl();
        BrokerImpl broker2 = new BrokerImpl();
        BrokerImpl broker3 = new BrokerImpl();
        //setName is the Threads.setName() method
        broker1.setName("Broker1");
        broker2.setName("Broker2");
        broker3.setName("Broker3");
        brokers.add(broker1);
        brokers.add(broker2);
        brokers.add(broker3);
        broker1.start();
        broker2.start();
        broker3.start();
    }
    static int port = 3321;
    public void run() {
        try {
            serverSocket = new ServerSocket(port+=1234, 6);
            String broker_hash = serverSocket.getInetAddress().getLocalHost().getHostAddress();
            String localport = String.valueOf(serverSocket.getLocalPort());
            broker_hash += ":" + localport;
            int brokersHash = calculateKeys(broker_hash);
            brokersHashCodes.put(this.getName(), brokersHash);
            System.out.println(brokersHashCodes);

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
}
