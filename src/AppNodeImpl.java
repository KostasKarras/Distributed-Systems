import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ObjIntConsumer;

public class AppNodeImpl implements Publisher, Consumer{

    private static Socket requestSocket;
    private static ObjectOutputStream objectOutputStream;
    private static ObjectInputStream objectInputStream;

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
    public void push(String hashtags, VideoFile video) {

        ArrayList<byte[]> chunks = generateChunks(video.getFilepath());//feygei

        try {

            objectOutputStream.writeObject(1);
            objectOutputStream.flush();

            objectOutputStream.writeObject(chunks.size());
            objectOutputStream.flush();

            while (!chunks.isEmpty()) {
                System.out.println("Size of ArrayList: " + chunks.size());
                byte[] clientToServer = chunks.remove(0);
                System.out.println("Size clientToServer: " + clientToServer.length);
                objectOutputStream.write(clientToServer);
                objectOutputStream.flush();
            }
            objectOutputStream.writeObject("1");
            objectOutputStream.flush();
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
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
    public void initialize(int port) {

        try {
            requestSocket = connect();
            objectOutputStream = new ObjectOutputStream(requestSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(requestSocket.getInputStream());
        }
        catch (IOException e){
            e.printStackTrace();
        }
        VideoFile vf = new VideoFile("C:\\Users\\miked\\Videos\\Captures\\Numb (Official Video) - Linkin Park - YouTube - Google Chrome 2020-04-03 14-10-06.mp4");
        push("#TIPOTES", vf);

        disconnect();

    }

    @Override
    public List<Broker> getBrokers() {
        return null;
    }

    @Override
    public Socket connect() {

        Socket requestSocket = null;

        try {
            requestSocket = new Socket(InetAddress.getByName("127.0.0.1"), 4321);
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return requestSocket;

    }

    public void disconnect() {
        try {
            objectOutputStream.writeObject(0);
            objectOutputStream.flush();

            objectInputStream.close();
            objectOutputStream.close();
            requestSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("Socket disconnected!");
        }
    };

    @Override
    public void updateNodes() {

    }

    public static void main(String[] args) {

        new AppNodeImpl().initialize(4321);

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
