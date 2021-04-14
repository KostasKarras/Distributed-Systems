import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

interface Node {

    static List<Broker> brokers = new ArrayList<>();

    public void init() throws UnknownHostException;

    public List<Broker> getBrokers();

    public Socket connect();

    public void disconnect();

    public void updateNodes();

}
