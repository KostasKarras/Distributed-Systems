import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

interface Node {

    static List<Broker> brokers = new ArrayList<>();

    public void initialize(int port) throws UnknownHostException;

    public List<Broker> getBrokers();

    public void connect();

    public void disconnect();

    public void updateNodes();

}