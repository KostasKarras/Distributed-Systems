import java.util.ArrayList;
import java.util.List;

public interface Publisher extends Node{

    ChannelName channelName = null;

    public void addHashTag(String hashtag);

    public void removeHashTag(String hashtag);

    public Broker hashTopic(String hashtopic);

    public void push(String hashtags, VideoFile video);

    public void notifyFailure(Broker broker);

    public void notifyBrokersForHashTags(String hashtag);

    public ArrayList <byte[]> generateChunks(VideoFile video);

}