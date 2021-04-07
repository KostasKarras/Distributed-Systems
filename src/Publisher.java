import java.util.ArrayList;
import java.util.List;

public class Publisher {

    private ChannelName channelName;

    public Publisher(ChannelName channelName){
        this.channelName = channelName;
    }

    public void addHashTag(String hashtag){

    }

    public void removeHashTag(String hashtag){

    }

    public List<Broker> getBrokerList(){

    }

    public Broker hashTopic(String hashtopic){

    }

    public void push(String hashtags, Value video){

    }

    public void notifyFailure(Broker broker){

    }

    public void notifyBrokersForHashTags(String hashtag){

    }

    public ArrayList<Value> generateChunks(String chunk){

    }
}