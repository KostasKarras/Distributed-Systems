import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class AppNodeImpl implements Publisher, Consumer{

    private static Socket requestSocket;
    private static ObjectOutputStream objectOutputStream;
    private static ObjectInputStream objectInputStream;
    private static ChannelName channel;

    @Override
    public void addHashTag(String hashtag) {
        channel.addHashTag(hashtag);
    }

    @Override
    public void removeHashTag(String hashtag) {
        channel.removeHashTag(hashtag);
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
    public void push(String hashtags, VideoFile video) {

        ArrayList<byte[]> chunks = generateChunks(video);
        String message;

        connect();

        try {

            objectOutputStream.writeObject(1);
            objectOutputStream.flush();

            objectOutputStream.writeObject("I want to push a new video!");
            objectOutputStream.flush();

            message = (String) objectInputStream.readObject();
            System.out.println("Server>" + message);

            objectOutputStream.writeObject(chunks.size());
            objectOutputStream.flush();

            objectOutputStream.writeObject(hashtags);
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

                disconnect();
                addHashTag(hashtags);
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
    public ArrayList<byte[]> generateChunks(VideoFile video) {
        ArrayList<byte[]> my_arraylist = new ArrayList<byte []>();

        boolean flag = true;
        int i = 0;
        byte[] inputBuffer = video.getVideoFileChunk();

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

        channel = new ChannelName("user");

        ArrayList<String> videoHashtags = new ArrayList<>();
        videoHashtags.add("First File");

        VideoFile vf = new VideoFile("C:\\Users\\Kostas\\IdeaProjects\\Distributed Systems\\src\\DIMAKHS.mp4", videoHashtags);
        push("#TIPOTES", vf);

    }

    @Override
    public List<Broker> getBrokers() {
        return null;
    }

    @Override
    public void connect() {

        try {
            requestSocket = new Socket(InetAddress.getByName("127.0.0.1"), 4321);
            objectOutputStream = new ObjectOutputStream(requestSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(requestSocket.getInputStream());
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void disconnect() {
        try {
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

    /**KOSTAS*/
    public static void main(String[] args) {
        new AppNodeImpl().initialize(4321);

        AppNodeImpl consumer = new AppNodeImpl();
        ArrayList<String> associatedHashtags = new ArrayList<String>();
        //I make a new empty constructor, because the is no reason to have the video file details
        VideoFile vf = new VideoFile();
        //If the topic is #TIPOTES then the playData method is working
        //But if you change that (even a letter) then the file isn't pulled
        //Because the publisher pushed the video with hashtag #TIPOTES
        consumer.playData("#TIPOTES", vf);
    }

    @Override
    public void register(Broker broker, String str) {

    }

    @Override
    public void disconnect(Broker broker, String str) {

    }

    /**KOSTAS*/
    @Override
    public void playData(String topic, VideoFile video) {
        String message;

        connect();

        try {

            //Just to wait the publisher push the video(It is for testing...)
            Thread.sleep(10000);

            //Handle pull code equals to 2
            objectOutputStream.writeObject(2);
            objectOutputStream.flush();

            objectOutputStream.writeObject("I want to pull a video!");
            objectOutputStream.flush();

            byte[] chunk;
            ArrayList<byte[]> chunks = new ArrayList<byte[]>();

            int size = (int) objectInputStream.readObject();

            //Sends topic to server
            objectOutputStream.writeObject(topic);
            objectOutputStream.flush();

            message = (String) objectInputStream.readObject();
            System.out.println("Server>" + message);

            message = (String) objectInputStream.readObject();
            System.out.println("Server>" + message);

            for (int i = 0; i < size; i++) {
                chunk = new byte[4096];
                chunk = objectInputStream.readAllBytes();
                chunks.add(chunk);
            }

            String videoExists = (String)objectInputStream.readObject();
            //The new file, test, in Desktop created only if the video exists.
            if(videoExists.equals("true")) {
                try {
                    File nf = new File("C:\\Users\\Kostas\\Desktop\\test.mp4");
                    for (byte[] ar : chunks) {
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
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }
}
