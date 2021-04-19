import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.ObjIntConsumer;

public class AppNodeImpl implements Publisher, Consumer{

    private static Socket requestSocket;
    private static ObjectOutputStream objectOutputStream; // MAY DELETE LATER or PASS THEM TO THE CONSUMER HANDLER
    private static ObjectInputStream objectInputStream;  // SAME
    private static ChannelName channel;

    public static void main(String[] args) {
        new AppNodeImpl().initialize(4321);
    }

    @Override
    public void initialize(int port) {
        

        channel = new Channel("USER");

        new PublisherHandler().start();

        runUser();

        //THINK FOR TOMMOROW THE IMPLEMENTATION!

        /**
        channel = new ChannelName("user");

        ArrayList<String> videoHashtags = new ArrayList<>();
        videoHashtags.add("First File");

        VideoFile vf = new VideoFile("C:\\Users\\miked\\Videos\\Captures\\Numb (Official Video) - Linkin Park - YouTube - Google Chrome 2020-04-03 14-10-06.mp4", videoHashtags);
        push("#TIPOTES", vf);
        */
    }

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
        handle_multicast();

        ArrayList<String> videoHashtags = new ArrayList<>();
        videoHashtags.add("First File");

        VideoFile vf = new VideoFile("C:\\Users\\miked\\Videos\\Captures\\Numb (Official Video) - Linkin Park - YouTube - Google Chrome 2020-04-03 14-10-06.mp4", videoHashtags);
        push("#TIPOTES", vf);

    }

    //THIS FUNCTION WAS USED FOR TESTING MULTICAST SOCKET
    public void handle_multicast() {

        try {

            MulticastSocket multicastSocket;

            //INITIALIZE MULTICAST SOCKET
            int multicastPort = 5000;
            InetAddress AppNodeIP = InetAddress.getByName("192.168.2.51");
            SocketAddress multicastSocketAddress = new InetSocketAddress(AppNodeIP, multicastPort);
            multicastSocket = new MulticastSocket(multicastSocketAddress);

            //JOIN GROUP ADDRESS
            InetAddress group_address = InetAddress.getByName("228.5.6.10");
            multicastSocket.joinGroup(group_address);

            //SEND PACKET
            DatagramPacket send_packet = new DatagramPacket("Good Morning".getBytes(), "Good Morning".length(), group_address, multicastPort);
            multicastSocket.send(send_packet);

            //CLOSE MULTICAST SOCKET
            multicastSocket.leaveGroup(group_address);
            multicastSocket.close();

        }
        catch (UnknownHostException uhe) {
            uhe.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }
    //

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

    @Override
    public void register(Broker broker, String str) {

    }

    @Override
    public void disconnect(Broker broker, String str) {

    }

    @Override
    public void playData(String str, VideoFile video) {

    }

    class RequestHandler extends Thread {
        
        RequestHandler() {}

        public void run() {}
    }

    class ServeRequest extends Thread {

        ServeRequest() {}

        public void run() {
            //OPTIONS
        }
    }

    public void runUser() {
        //BUILD INTERFACE
    }
}
