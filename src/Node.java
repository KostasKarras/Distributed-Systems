import java.net.UnknownHostException;
import java.util.List;

interface Node {

    static final List<Broker> brokers = null;

    public void init(int i) throws UnknownHostException;

    public List<Broker> getBrokers();

    public void connect();

    public void disconnect();

    public void updateNodes();

}
