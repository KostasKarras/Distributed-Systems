import java.util.List;

public interface Broker extends Node{

    static List<Consumer> registeredUsers = null;
    static List<Publisher> registeredPublishers = null;

    public int calculateKeys(String ID);

    public Publisher acceptConnection(Publisher publisher);

    public Consumer acceptConnection(Consumer consumer);

    public void notifyPublisher(String topic);

    public void notifyBrokersOnChanges();

    public void pull(String topic);

    public void filterConsumers(String user_id);
}