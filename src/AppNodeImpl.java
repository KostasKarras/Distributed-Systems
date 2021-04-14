import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AppNodeImpl implements Publisher, Consumer{

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
    public ArrayList<byte[]> generateChunks(String filepath) {
        ArrayList<byte[]> my_arraylist = new ArrayList<byte []>();

        VideoFile vf = new VideoFile(filepath);

        boolean flag = true;
        int i = 0;
        byte[] inputBuffer = vf.getVideoFileChunk();

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
    public void init() {

    }

    @Override
    public List<Broker> getBrokers() {
        return null;
    }

    @Override
    public Socket connect() {
        return null;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void updateNodes() {

    }

    public static void main(String[] args) {
        Publisher p = new AppNodeImpl();
        //generateChunks argument is the filepath
        ArrayList<byte[]> inputArraylist = p.generateChunks("C:\\Users\\miked\\Videos\\Captures\\Numb (Official Video) - Linkin Park - YouTube - Google Chrome 2020-04-03 14-10-06.mp4");
        try {
            File nf = new File("C:/Users/miked/Desktop/test.mp4");
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

    @Override
    public void register(Broker broker, String str) {

    }

    @Override
    public void disconnect(Broker broker, String str) {

    }

    @Override
    public void playData(String str, Value video) {

    }
}
