import java.util.List;

public class Broker {

    private List<Consumer> registerUsers;
    private List<Publisher> registerPublishers;

    public Broker(List<Consumer> registerUsers, List<Publisher> registerPublishers){
        this.registerUsers = registerUsers;
        this.registerPublishers = registerPublishers;
    }

    public void calculateKeys(){

    }

    public Publisher acceptConnection(Publisher publisher){

    }

    public Consumer acceptConnection(Consumer consumer){

    }

    public void notifyPublisher(String ){

    }

    public void notifyBrokersOnChanges(){

    }

    public void pull(String channel_or_hashtag){

    }

    public void filterConsumers(String ){

    }
}