import java.util.ArrayList;
import java.util.List;

public class AppNodeImpl implements Publisher, Consumer{
    
    @Override
    public void register(Broker broker) {

    }

    @Override
    public void disconnect(Broker broker) {

    }

    @Override
    public void playData(Value video) {

    }

    @Override
    public void addHashTag(String hashtag) {

    }

    @Override
    public void removeHashTag(String hashtag) {

    }

    @Override
    public List<Broker> getBrokerList() {
        return null;
    }

    @Override
    public Broker hashTopic(String hashtopic) {
        return null;
    }

    @Override
    public void push(String hashtags, Value video) {

    }

    @Override
    public void notifyFailure(Broker broker) {

    }

    @Override
    public void notifyBrokersForHashTags(String hashtag) {

    }

    @Override
    public ArrayList<Value> generateChunks(String chunk) {
        return null;
    }

    @Override
    public void init(int i) {

    }

    @Override
    public List<Broker> getBrokers() {
        return null;
    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void updateNodes() {

    }
}
