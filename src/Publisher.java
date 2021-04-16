import java.util.ArrayList;
import java.util.List;

interface Publisher extends Node{

    static final ChannelName channelName = null;

    public void addHashTag(String hashtag, Value video);

    public void removeHashTag(String hashtag);

    public List<Broker> getBrokerList();

    public Broker hashTopic(String hashtopic);

    public void push(String hashtags, Value video);

    public void notifyFailure(Broker broker);

    public void notifyBrokersForHashTags(String hashtag);

    public ArrayList<byte[]> generateChunks(String filepath, ArrayList<String> hashtags);
}
