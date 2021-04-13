import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class BrokerImpl implements Broker{

    public int ID;

    @Override
    public void calculateKeys() {

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
    public void init(int i) throws UnknownHostException {

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

    }

    @Override
    public List<Broker> getBrokers() {
        return null;
    }

    @Override
    public void connect() {

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
