import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;

public class AppNodeImpl implements Publisher, Consumer {

    /** Main Method */
    public static void main(String[] args) {
        AppNodeImpl Node = new AppNodeImpl();
    }

    public void addHashTag(String hashtag) {

    }

    public void removeHashTag(String hashtag) {

    }

    public Broker hashTopic(String hashtopic) {
        return null;
    }

    public void push(String hashtags, VideoFile video) {
        ArrayList<byte[]> chunks = generateChunks(video);
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

    public void notifyFailure(Broker broker) {

    }

    public void notifyBrokersForHashTags(String hashtag) {

    }

    public ArrayList <byte[]> generateChunks(VideoFile video) {
        final int CHUNK_SIZE = 4096;
        ArrayList <byte[]> chunkArrayList = new ArrayList <> ();
        int byteFilePointer = 0;
        byte[] videoBuffer = video.getVideoFileChunk();

        while (byteFilePointer < videoBuffer.length) {
            byte[] chunk = new byte[CHUNK_SIZE];
            for (int j = 0; j < chunk.length; j++) {
                if (byteFilePointer < videoBuffer.length)
                    chunk[j] = videoBuffer[byteFilePointer++];
            }
            chunkArrayList.add(chunk);
        }
        return chunkArrayList;
    }

    public void register(Broker broker, String str) {

    }

    public void playData(String str, VideoFile video) {

    }

    public void initialize(int i) {

    }

    public List<Broker> getBrokers() {
        return null;
    }

    public Socket connect() {
        Socket requestSocket = null;

        try {
            requestSocket = new Socket("127.0.0.1", 4321);
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return requestSocket;
    }

    public void disconnect(Socket s) {
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateNodes() {

    }
}