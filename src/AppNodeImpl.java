import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class AppNodeImpl extends Thread implements Publisher, Consumer{
    @Override
    public void push(String hashtags, Value video) {
        this.start();
        video.addAssociatedHashTags(hashtags);
        this.addHashTag(hashtags);
    }

    Value value;
    String type;
    public AppNodeImpl(String type, Value value){
        this.type = type;
        this.value = value;
    }

    public ArrayList<byte[]> generateChunks(String filepath, ArrayList<String> hashtags) {
        ArrayList<byte[]> my_arraylist = new ArrayList<byte []>();

        VideoFile vf = new VideoFile(filepath, hashtags);

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

    public void run(){
        ArrayList<byte[]> chunks = generateChunks(this.getValue().getFilepath(), this.getValue().getAssociatedHashtags());
        Socket requestSocket = null;
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        String message;

/*
-----------------------------------
PORTS TO CONNECT THE REQUEST SOCKET
-----------------------------------
----------
PORT: 7023
PORT: 5789
PORT: 4555
----------
*/

        try {
            requestSocket = new Socket(InetAddress.getByName("127.0.0.1"), 4555);

            objectOutputStream = new ObjectOutputStream(requestSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(requestSocket.getInputStream());

            try {
                objectOutputStream.writeObject("Hello! New Client here!");
                objectOutputStream.flush();

                message = (String) objectInputStream.readObject();
                System.out.println("Server>" + message);

                objectOutputStream.writeObject(chunks.size());
                objectOutputStream.flush();

                while (!chunks.isEmpty()) {
                    byte[] clientToServer = chunks.remove(0);
                    objectOutputStream.write(clientToServer);
                    objectOutputStream.flush();
                }
            } catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host!");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    objectOutputStream.writeObject("Bye");
                    objectOutputStream.flush();
                    objectInputStream.close();
                    objectOutputStream.close();
                    requestSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        //generateChunks argument is the filepath
//        ArrayList<byte[]> inputArraylist = p.generateChunks("C:\\Users\\Kostas\\IdeaProjects\\Distributed Systems\\src\\DIMAKHS.mp4");
//        try {
//            File nf = new File("C:/Users/Kostas/Desktop/test.mp4");
//            for (byte[] ar : inputArraylist) {
//                FileOutputStream fw = new FileOutputStream(nf, true);
//                try {
//                    fw.write(ar);
//                } finally {
//                    fw.close();
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        ArrayList<String> hashtags1 = new ArrayList<String>();
        hashtags1.add("First File");
        VideoFile vf = new VideoFile("C:\\Users\\Kostas\\Downloads\\yt1s.com - Dji Mavic mini  Video test cinematic_720p.mp4", hashtags1);
        Value v = new Value(vf);
        //p.push("#TIPOTES", v);
        Publisher p = new AppNodeImpl("p", v);
        p.push("#TIPOTES", v);

        ArrayList<String> hashtags2 = new ArrayList<String>();
        hashtags2.add("Second File");
        VideoFile vf2 = new VideoFile("C:\\Users\\Kostas\\IdeaProjects\\Distributed Systems\\src\\DIMAKHS.mp4", hashtags2);
        Value v2 = new Value(vf2);
        //p2.push("#TIPOTES", v2);
        Publisher p2 = new AppNodeImpl("p", v2);
        p2.push("#TIPOTES", v2);
    }

    public Value getValue(){
        return this.value;
    }

    //Hashtags for which the publisher is responsible
    ArrayList<String> hashtags = new ArrayList<String>();
    @Override
    public void addHashTag(String hashtag) {
        hashtags.add(hashtag);
    }

    @Override
    public void removeHashTag(String hashtag) {
        hashtags.remove(hashtag);
    }

    @Override
    public List<Broker> getBrokerList() {
        return brokers;
    }

    @Override
    public Broker hashTopic(String hashtopic) {
        return null;
    }

    @Override
    public void notifyFailure(Broker broker) {

    }

    @Override
    public void notifyBrokersForHashTags(String hashtag) {

    }

    @Override
    public void init() {

    }

    @Override
    public List<Broker> getBrokers() {
        return brokers;
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
