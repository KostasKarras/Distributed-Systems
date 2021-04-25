interface Consumer extends Node{

    public void register(Broker broker, String str);

    public void disconnect(Broker broker, String str);//MALLON AXRISTO?

    public void playData(String str, VideoFile video);

}
