public interface Consumer extends Node {

    public void register(Broker broker, String id);

    public void playData(String name, VideoFile video);

}