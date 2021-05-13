import java.util.HashMap;
import java.util.List;

interface Broker extends Node {

    static final List<Consumer> registerUsers = null;
    static final List<Publisher> registerPublishers = null;

    public int calculateKeys(String id);

//    public Publisher acceptConnection(Publisher publisher);
//
//    public Consumer acceptConnection(Consumer consumer);
//
//    public void notifyPublisher(String str);

//    public void notifyBrokersOnChanges();

    public HashMap<ChannelKey, String> filterConsumers(HashMap<ChannelKey, String> videoList, String channelName);//DEN XRHSIMOPOIEITAI

}
