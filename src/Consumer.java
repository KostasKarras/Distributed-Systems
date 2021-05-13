import java.net.SocketAddress;
import java.util.HashMap;

interface Consumer extends Node{

    public void register(SocketAddress socketAddress, String topic);

    public void disconnect(SocketAddress socketAddress, String topic);

    public void playData(HashMap<ChannelKey, String> videoList);

}