import javax.naming.ldap.SortKey;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class AppNodeImpl implements Publisher, Consumer{

    private static Socket requestSocket;
    private static ObjectOutputStream objectOutputStream; // MAY DELETE LATER or PASS THEM TO THE CONSUMER HANDLER
    private static ObjectInputStream objectInputStream;  // SAME
    private static Channel channel;

    private static TreeMap<Integer, SocketAddress> brokerHashes = new TreeMap<>();


    public static void main(String[] args) {
        new AppNodeImpl().initialize(4321);
    }

    @Override
    public void initialize(int port) {

        channel = new Channel("USER");

        new RequestHandler().start();

        runUser();

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
    public ArrayList<Broker> getBrokerList() {
        return null;
    }

    @Override
    public SocketAddress hashTopic(String hashtopic) {

        int digest;
        SocketAddress brokerAddress = brokerHashes.get(brokerHashes.firstKey());
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] bb = sha256.digest(hashtopic.getBytes(StandardCharsets.UTF_8));
            BigInteger bigInteger = new BigInteger(1, bb);
            digest = bigInteger.intValue();

            //Fit to the right broker
            for (int hash : brokerHashes.keySet()) {
                if (digest <= hash) {
                    brokerAddress = brokerHashes.get(hash);
                    break;
                }
            }
        }
        catch (NoSuchAlgorithmException nsae) {
            nsae.printStackTrace();
        }
        finally {
            return brokerAddress;
        }
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
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
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
    public void notifyBrokersForHashTags(String hashtag, String action) {//find apropriate broker by using hashtopic first.
        /**KOSTAS-START*/
        //klhsh sth hashtopic -> SocketAddress
        //syndesi sto socketAddress
        /**DIMITRIS*/
        SocketAddress socketAddress = hashTopic(hashtag);
        connect(socketAddress);
        try {
            objectOutputStream.writeObject(7);
            objectOutputStream.writeObject(action);
            objectOutputStream.writeObject(hashtag);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            disconnect();
        }
        /**KOSTAS-END*/
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
    public TreeMap<Integer, SocketAddress> getBrokerMap() {
        /**DIMITRIS*/
        connect();
        try {
            objectOutputStream.writeObject(0);
            brokerHashes = (TreeMap<Integer, SocketAddress>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            return brokerHashes;
        }
    }

    private void connect(SocketAddress socketAddress) {
        try {
            requestSocket.connect(socketAddress);
            objectOutputStream = new ObjectOutputStream(requestSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(requestSocket.getInputStream());
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host.");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void sendChannelVideoList(ServeRequest serveRequest) {
        try{
            serveRequest.objectOutputStream.writeObject(channel.getChannelVideoNames());
            /*
            HashMap<Integer, String> test = new HashMap<>();
            test.put(10, "Michalis");
            test.put(20, "George");
            test.put(30, "Grace");
            serveRequest.objectOutputStream.writeObject(test);
            */
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    //CHANGES HAVE BEEN MADE
    class RequestHandler extends Thread {

        public ServerSocket serverSocket;
        public Socket connectionSocket;
        private static final int port = 4900;
        private int current_threads = 1;

        public void run() {

            try {
                serverSocket = new ServerSocket(port);

                while(true) {
                    connectionSocket = serverSocket.accept();
                    new ServeRequest(connectionSocket, current_threads).start();
                    current_threads++;
                }
            } catch(IOException e) {
                /* Crash the server if IO fails. Something bad has happened. */
                throw new RuntimeException("Could not create ServerSocket ", e);
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    class ServeRequest extends Thread {

        private Socket socket;
        private int threadNumber;
        private ObjectInputStream objectInputStream;
        private ObjectOutputStream objectOutputStream;

        ServeRequest(Socket s, int currentThreads) {
            requestSocket = s;
            threadNumber = currentThreads;
            setName("Thread " + threadNumber);
            try {
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectInputStream = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try{

                int option = (int) objectInputStream.readObject();

                if (option == 1) { //Pull

                    //Choice between sending whole channel or files based on hashtag
                    String choice = (String) objectInputStream.readObject();
                    System.out.println(choice);
                    if (choice.equals("CHANNEL")) {
                        sendChannelVideoList(this);
                    }
                    else {
                    }

                } else if (option == 2) { // Notify Publisher (Broker sends Publisher the keys he is responsible for)

                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    objectInputStream.close();
                    objectOutputStream.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }//?
        }
    }

    private void runUser() {
        //BUILD INTERFACE
        Scanner in = new Scanner(System.in);
        int end = 0;
        String choice;
        do {
            System.out.println("===== Menu =====");
            //Consumer Methods
            System.out.println("1. Subscribe to Topic or Channel");
            System.out.println("2. Get Topic Video List");
            System.out.println("3. Play Data");
            //Publisher Methods
            System.out.println("4. Add Hashtags to a Video");
            System.out.println("5. Remove Hashtags from a Video");
            System.out.println("6. Upload Video");
            System.out.println("7. Delete Video");
            System.out.println("0. Exit");
            choice = in.nextLine();
            if (choice.equals("1")) {
                /**DIMITRIS*/
                String topic;
                System.out.print("Please select a topic (hashtag/channel) that you want to subscribe: ");
                topic = in.nextLine();

                SocketAddress socketAddress = hashTopic(topic);
                connect(socketAddress);

                try {
                    objectOutputStream.writeObject(1);
                    objectOutputStream.flush();
                    objectOutputStream.writeObject(topic);
                    objectOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (choice.equals("2")) {

                /**DIMITRIS*/
                String topic;
                System.out.print("Please give a topic (hashtag/channel) that you want to search results for: ");
                topic = in.nextLine();

                SocketAddress socketAddress = hashTopic(topic);
                connect(socketAddress);

                try {
                    objectOutputStream.writeObject(2);
                    objectOutputStream.writeObject(topic);

                    Object [][] topicVideoList = (Object[][]) objectInputStream.readObject();//ARRAY, HASHMAP?
                    System.out.println(Arrays.toString(topicVideoList));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

            } else if (choice.equals("3")) {

                String channelName;
                int videoID;
                System.out.print("Please give a topic (hashtag/channel) that you want to search results for: ");
                channelName = in.nextLine();
                videoID = Integer.parseInt(in.nextLine());

                try {
                    objectOutputStream.writeObject(3);
                    objectOutputStream.writeObject(channelName);
                    objectOutputStream.writeObject(videoID);

                    //STREAM VIDEO
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (choice.equals("4")) {

                int videoID;
                String hashtag;
                ArrayList<String> hashtags = new ArrayList<>();

                if (channel.getID_VideoFileMap().isEmpty()) {
                    System.out.println("The channel doesn't have any videos to add hashtags.");
                    continue;
                }

                System.out.println(channel.toString());

                System.out.print("Please give the videoID of the video you want to add a hashtag: ");
                videoID = Integer.parseInt(in.nextLine());

                VideoFile video = channel.getVideoFile_byID(videoID);

                while (true) {
                    System.out.print("Do you want to add a hashtag to this video? (y/n) ");
                    String answer = in.nextLine();
                    if (answer.equals("n")) {
                        break;
                    }

                    System.out.print("Please give the hashtag that you want to add: ");
                    hashtag = in.nextLine();

                    if (!hashtags.contains(hashtag) && !video.getAssociatedHashtags().contains(hashtag)) {
                        hashtags.add(hashtag);
                    }
                }

                if (hashtags.isEmpty()) {
                    System.out.println("No hashtags found to add.");
                    continue;
                }

                channel.updateVideoFile(video, hashtags, "ADD", this);

            } else if (choice.equals("5")) {

                int videoID;
                String hashtag;
                ArrayList<String> hashtags = new ArrayList<>();

                if (channel.getID_VideoFileMap().isEmpty()) {
                    System.out.println("The channel doesn't have any videos to remove hashtags.");
                    continue;
                }

                System.out.println(channel.toString());

                System.out.print("Please give the videoID of the video you want to remove a hashtag: ");
                videoID = Integer.parseInt(in.nextLine());

                VideoFile video = channel.getVideoFile_byID(videoID);

                while (true) {
                    System.out.print("Do you want to remove a hashtag to this video? (y/n) ");
                    String answer = in.nextLine();
                    if (answer.equals("n")) {
                        break;
                    }

                    System.out.print("Please give the hashtag that you want to remove: ");
                    hashtag = in.nextLine();

                    if (!hashtags.contains(hashtag) && video.getAssociatedHashtags().contains(hashtag)) {
                        hashtags.add(hashtag);
                    }
                }

                if (hashtags.isEmpty()) {
                    System.out.println("No hashtags found to remove.");
                    continue;
                }

                channel.updateVideoFile(video, hashtags, "REMOVE", this);

            } else if (choice.equals("6")) {

                String filepath;
                String videoTitle;
                String hashtag;
                ArrayList<String> associatedHashtags = new ArrayList<>();

                System.out.print("Please give the path of the video you want to upload: ");
                filepath = in.nextLine();

                System.out.print("Title of the video: ");
                videoTitle = in.nextLine();

                while (true) {
                    System.out.print("Do you want to add a hashtag to your video? (y/n) ");
                    String answer = in.nextLine();
                    if (answer.equals("n")) {
                        break;
                    }

                    System.out.print("Please give a hashtag for the video: ");
                    hashtag = in.nextLine();

                    if (!associatedHashtags.contains(hashtag)) {
                        associatedHashtags.add(hashtag);
                    }
                }

                VideoFile video = new VideoFile(filepath, associatedHashtags, videoTitle);
                channel.addVideoFile(video, this);

            } else if (choice.equals("7")){

                int videoID;

                if (channel.getID_VideoFileMap().isEmpty()) {
                    System.out.println("The channel doesn't have any videos to delete.");
                    continue;
                }

                System.out.println(channel.toString());

                System.out.print("Please give the ID of the video you want to delete: ");
                videoID = Integer.parseInt(in.nextLine());

                VideoFile video = channel.getVideoFile_byID(videoID);

                channel.removeVideoFile(video, this);

            } else if (choice.equals("0")) {
                end = 1;
            }
        } while (end == 0);
    }
}