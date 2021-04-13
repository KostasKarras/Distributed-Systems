import java.util.List;

interface Broker extends Node{

    static final List<Consumer> registerUsers = null;
    static final List<Publisher> registerPublishers = null;

    public void calculateKeys();

    public Publisher acceptConnection(Publisher publisher);

    public Consumer acceptConnection(Consumer consumer);

    public void notifyPublisher(String );

    public void notifyBrokersOnChanges();

    public void pull(String channel_or_hashtag);

    public void filterConsumers(String );
}
