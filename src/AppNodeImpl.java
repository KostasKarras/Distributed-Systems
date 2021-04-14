import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
        ArrayList<byte[]> chunks = generateChunks(video.getFilepath());
        Socket requestSocket = null;
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        String message;

        try {
            requestSocket = new Socket(InetAddress.getByName("127.0.0.1"), 4321);

            objectOutputStream = new ObjectOutputStream(requestSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(requestSocket.getInputStream());

            try {
                message = (String) objectInputStream.readObject();
                System.out.println("Server>" + message);

                objectOutputStream.writeObject("Hi!");
                objectOutputStream.flush();

                while (!chunks.isEmpty()) {
                    System.out.println("Size of ArrayList: " + chunks.size());
                    byte[] clientToServer = chunks.remove(0);
                    objectOutputStream.writeObject(clientToServer);
                    objectOutputStream.flush();
                }

                objectOutputStream.writeObject("Just Testing..");
                objectOutputStream.flush();

                objectOutputStream.writeObject("Socket lab testing");
                objectOutputStream.flush();

                objectOutputStream.writeObject("bye");
                objectOutputStream.flush();

            } catch (ClassNotFoundException classNot) {
                System.err.println("data received in unknown format");
            }
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                objectInputStream.close();
                objectOutputStream.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
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
        //generateChunks argument is the filepath
        ArrayList<byte[]> inputArraylist = p.generateChunks("C:\\Users\\Kostas\\IdeaProjects\\Distributed Systems\\src\\DIMAKHS.mp4");
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
        VideoFile vf = new VideoFile("C:\\Users\\Kostas\\Downloads\\yt1s.com - Dji Mavic mini  Video test cinematic_720p.mp4");
        Value v = new Value(vf);
        p.push("#TIPOTES", v);
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