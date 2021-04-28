import java.util.ArrayList;
import java.util.List;
import java.net.SocketAddress;

interface Publisher extends Node{

    static final Channel channel = null;

    public void addHashTag(String hashtag);

    public void removeHashTag(String hashtag);

    public List<Broker> getBrokerList();

    public SocketAddress hashTopic(String hashtopic);

    public void push(String hashtags, VideoFile video);

    public void notifyFailure(Broker broker);

    public void notifyBrokersForHashTags(String hashtag, String action);

    public ArrayList<byte[]> generateChunks(VideoFile video);
}