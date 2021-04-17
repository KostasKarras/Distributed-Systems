import java.util.List;
import java.net.Socket;

public interface Node {

    static List<Broker> brokers = null;

    public void initialize(int port);

    public List <Broker> getBrokers();

    public Socket connect();

    public void disconnect(Socket s);

    public void updateNodes();

}