import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.util.ArrayList;

interface Publisher extends Node{

    static final Channel channel = null;

    public void addHashTag(VideoFile video);

    public void removeHashTag(VideoFile video);

//    public List<Broker> getBrokerList();

    public SocketAddress hashTopic(String hashtopic);

    public void push(int id, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream);

//    public void notifyFailure(Broker broker); MALLON THA FYGEI

    public void notifyBrokersForHashTags(String hashtag, String action);

    public ArrayList<byte[]> generateChunks(VideoFile video);
}