import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
        ArrayList<byte[]> my_arraylist = new ArrayList<byte []>();

        //to epomeno arraylist kai byte array einai mono gia thn arxikopoihsh tou VideoFile
        ArrayList<String> empty = null;
        byte[] arr = null;
        VideoFile vf = new VideoFile("", "", "", "", "", "",
                "", empty, arr);

        boolean flag = true;
        int i = 0;
        byte[] inputBuffer = vf.getVideoFileChunk("C:\\Users\\Kostas\\IdeaProjects\\Distributed Systems\\src\\DIMAKHS.mp4");

        while (i < inputBuffer.length) {
            byte[] buffer = new byte[4096];
            for (int j = 0;j < buffer.length;j++) {
                if (i < inputBuffer.length)
                    buffer[j] = inputBuffer[i++];
            }
            my_arraylist.add(buffer);
        }
        return my_arraylist;
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

    public static void main(String[] args) {
        Publisher p = new AppNodeImpl();
        ArrayList<byte[]> inputArraylist = p.generateChunks();
        try {
            File nf = new File("C:/Users/Kostas/Desktop/test.mp4");
            for (byte[] ar : inputArraylist) {
                FileOutputStream fw = new FileOutputStream(nf, true);
                try {
                    fw.write(ar);
                } finally {
                    fw.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
