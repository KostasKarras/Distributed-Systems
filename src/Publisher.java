import java.util.ArrayList;
import java.util.List;

interface Publisher extends Node{

    static final Channel channel = null;

    public void addHashTag(String hashtag);

    public void removeHashTag(String hashtag);

    public List<Broker> getBrokerList();

    public Broker hashTopic(String hashtopic);

    public void push(String hashtags, VideoFile video);

    public void notifyFailure(Broker broker);

    public void notifyBrokersForHashTags(String hashtag);

    public ArrayList<byte[]> generateChunks(VideoFile video);
}
