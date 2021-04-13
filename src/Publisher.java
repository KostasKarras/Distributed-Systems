import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Publisher extends Node {

    public static final ChannelName channelName = null;
    public static final List<Broker> brokers = null;

    public void addHashTag(String hashtag) {

    }

    public void removeHashTag(String hashtag) {

    }

    public List<Broker> getBrokerList() {
        return super.getBrokers();
    }

//    public Broker hashTopic(String hashtopic){
//
//    }

    public void push(String hashtags, Value video) {

    }

    public void notifyFailure(Broker broker) {

    }

    public void notifyBrokersForHashTags(String hashtag) {

    }

    public ArrayList<byte[]> generateChunks() {
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

    public static void main(String[] args) {
        Publisher p = new Publisher();
        ArrayList<byte[]> inputArraylist = p.generateChunks();
        for(byte[] ar : inputArraylist)
            System.out.println(ar);
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
