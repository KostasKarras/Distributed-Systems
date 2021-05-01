import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

interface Node {

    static List<Broker> brokers = new ArrayList<>();

    public void initialize(int port) throws UnknownHostException;

    public TreeMap<Integer, SocketAddress> getBrokerMap();//DIMITRIS

    public void connect();

    public void disconnect();

    public void updateNodes();

}