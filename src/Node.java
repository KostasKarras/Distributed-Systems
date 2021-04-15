import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

interface Node {

    static List<Broker> brokers = new ArrayList<>();

    public void initialize(int port) throws UnknownHostException;

    public List<Broker> getBrokers();

    public Socket connect();

    public void disconnect(Socket s);

    public void updateNodes();

}
