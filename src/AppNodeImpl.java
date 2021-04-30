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

    private static Socket requestSocket2;
    private static ObjectOutputStream objectOutputStream2;
    private static ObjectInputStream objectInputStream2;

    //CHANGE
    private static ServerSocket serverSocket;
    private static TreeMap<Integer, SocketAddress> brokerHashes = new TreeMap<>();
    private static SocketAddress channelBroker;
    //

    public static void main(String[] args) {

        new AppNodeImpl().initialize(4950);
    }

    @Override
    public void initialize(int port) {

        //CHANNEL NAME
        channel = new Channel("USER");

        //FIRST CONNECTION
        connect();

        try {

            //THAT IS NOT CORRECT YET

            //SEND OPTION 4 FOR INITIALIZATION
            objectOutputStream.writeObject(4);
            objectOutputStream.flush();

            //RECEIVE BROKER HASHES
            brokerHashes = (TreeMap<Integer, SocketAddress>) objectInputStream.readObject();

            //SEND CHANNEL NAME
            objectOutputStream.writeObject(channel.getChannelName());
            objectOutputStream.flush();

            //SEND SOCKET ADDRESS FOR CONNECTIONS
            serverSocket = new ServerSocket(port);
            objectOutputStream.writeObject(serverSocket.getLocalSocketAddress());
            objectOutputStream.flush();

            //WHATEVER ELSE WE NEED

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }

        new RequestHandler().start();

        runUser();

        //THINK FOR TOMORROW THE IMPLEMENTATION!

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
    public void push(int id, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) throws NoSuchElementException {

        ArrayList<byte[]> chunks = generateChunks(channel.getVideoFile_byID(id));

        try {
            objectOutputStream.writeObject(true);
            objectOutputStream.flush();

            objectOutputStream.writeObject(chunks.size());
            objectOutputStream.flush();

            while (!chunks.isEmpty()) {
                byte[] clientToServer = chunks.remove(0);
                objectOutputStream.write(clientToServer);
                objectOutputStream.flush();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public void notifyFailure(Broker broker) {

    }

    /**KOSTAS-START*/
    @Override
    public void notifyBrokersForHashTags(String hashtag, String action) {
        connect();
        try {
            objectOutputStream.writeObject(7);
            objectOutputStream.flush();

            objectOutputStream.writeObject(hashtag);
            objectOutputStream.flush();

            objectOutputStream.writeObject(action);
            objectOutputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }
    /**KOSTAS-END*/

    /** Check if it runs, to simplify things */
    private void connect2(SocketAddress socketAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName("127.0.0.1");
            socketAddress = new InetSocketAddress(inetAddress, 4321);
            //requestSocket2.bind(socketAddress);
            requestSocket2.connect(socketAddress);
            objectOutputStream2 = new ObjectOutputStream(requestSocket2.getOutputStream());
            objectInputStream2 = new ObjectInputStream(requestSocket2.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**DIMITRIS-END*/

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
    public List<Broker> getBrokers() {
        return brokers;
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

    public void disconnect2() {
        try {
            objectInputStream2.close();
            objectOutputStream2.close();
            requestSocket2.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("Socket disconnected!");
        }
    }

    public void disconnect() {
        try {
            objectOutputStream.writeObject(-1);
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
    public void playData(String str, VideoFile video) {

    }

    public HashMap<Integer, String> getChannelVideoMap() {
        return channel.getChannelVideoNames();
    }

    public HashMap<ChannelKey, String> getHashtagVideoMap(String hashtag) {
        return channel.getChannelVideoNamesByHashtag(hashtag);
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
                } catch (IOException | NullPointerException ioException) {
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
            socket = s;
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

                if (option == 1) { //Pull List

                    //Choice between sending whole channel or files based on hashtag
                    String choice = (String) objectInputStream.readObject();
                    System.out.println(choice);
                    if (choice.equals("CHANNEL")) {
                        HashMap<Integer, String> videoList = getChannelVideoMap();
                        objectOutputStream.writeObject(videoList);
                    }
                    else {
                        HashMap<ChannelKey, String> videoList = getHashtagVideoMap(choice);
                        objectOutputStream.writeObject(videoList);
                    }

                } else if (option == 2) { //Pull Video(I DELETE STH THAT HAS TO DO WITH NOTIFICATION PUBLISHER, but i am not sure)
                    ChannelKey channelKey = (ChannelKey) objectInputStream.readObject();
                    try {
                        push(channelKey.getVideoID(), objectInputStream, objectOutputStream);
                    } catch (NoSuchElementException nsee) {
                        objectOutputStream.writeObject(false);
                        objectOutputStream.flush();
                    }
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
            }
        }
    }

    public void runUser() {
        //BUILD INTERFACE
        Scanner in = new Scanner(System.in);
        int end = 0;
        String choice;
        do {
            System.out.println("===== Menu =====");
            //Consumer Methods
            System.out.println("1. Register User");
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

            } else if (choice.equals("2")) {

                requestSocket2 = new Socket();

                //Give hashtag
                System.out.print("Please give the hashtag or the channel that you want to search for: ");
                String channel_or_hashtag = in.nextLine();

                //Get right broker
                SocketAddress socketAddress = hashTopic(channel_or_hashtag);
                //System.out.println(socketAddress);

                //Connect to that broker
                connect2(socketAddress);

                HashMap<ChannelKey, String> videoList = null;

                try {
                    //Write option
                    objectOutputStream2.writeObject(2);
                    objectOutputStream2.flush();

                    //Write channel name or hashtag
                    objectOutputStream2.writeObject(channel_or_hashtag);
                    objectOutputStream2.flush();

                    //Read videoList
                    videoList = (HashMap<ChannelKey, String>) objectInputStream2.readObject();
                    System.out.println(videoList);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    disconnect2();
                }

                //CHOOSE SOME VIDEO OR GO BACK
                boolean wantVideo = true;
                while (wantVideo) {
                    System.out.print("Do you want to see a video from these? (y/n)");
                    String answer = in.nextLine();

                    if (answer.equals("y")) {
                        try {
                            requestSocket2 = new Socket();

                            System.out.print("Give the Channel Name that you want to play: ");
                            String channelName = in.nextLine();

                            System.out.print("Give the video ID that you want to play: ");
                            int videoID = in.nextInt();

                            ChannelKey key = new ChannelKey(channelName, videoID);

                            //CONNECTING TO BROKER RESPONSIBLE FOR CHANNEL, THAT HAS THE VIDEO WE ASKED FOR
                            SocketAddress brokerAddress = hashTopic(channelName);
                            connect2(brokerAddress);

                            objectOutputStream2.writeObject(3);
                            objectOutputStream2.flush();

                            objectOutputStream2.writeObject(key);
                            objectOutputStream2.flush();

                            //CHECK IF CHANNEL NAME EXISTS
//                            String message = (String) objectInputStream2.readObject();
//                            System.out.println(message);

                            //RECEIVE VIDEO FILE CHUNKS
                            byte[] chunk;
                            ArrayList<byte[]> chunks = new ArrayList<byte[]>();
                            int size = (int) objectInputStream2.readObject();

                            if (size == 0) {
                                System.out.println("CHANNEL HAS NO VIDEO WITH THIS ID...");
                            }
                            //REBUILD CHUNKS FOR TESTING
                            else {
                                for (int i = 0; i < size; i++) {
                                    chunk = new byte[4096];
                                    chunk = objectInputStream2.readAllBytes();
                                    chunks.add(chunk);
                                }
                                try {
                                    File nf = new File("C:/Users/Kostas/Desktop/test.mp4");
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
                        } finally {
                            disconnect2();
                        }
                    }
                    else
                        wantVideo = false;
                }
            } else if (choice.equals("3")) {

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

                /**KOSTAS-START*/
                HashMap<String, String> notificationHashtags = channel.updateVideoFile(video, hashtags, "ADD");;
                if (!notificationHashtags.isEmpty()) {
                    for (Map.Entry<String, String> item : notificationHashtags.entrySet())
                        notifyBrokersForHashTags(item.getKey(), item.getValue());
                }
                /**KOSTAS-END*/
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

                /**KOSTAS-START*/
                HashMap<String, String> notificationHashtags = channel.updateVideoFile(video, hashtags, "REMOVE");
                if (!notificationHashtags.isEmpty()) {
                    for (Map.Entry<String, String> item : notificationHashtags.entrySet())
                        notifyBrokersForHashTags(item.getKey(), item.getValue());
                }
                /**KOSTAS-END*/

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
                /**KOSTAS-START*/
                HashMap<String, String> notificationHashtags = channel.addVideoFile(video);
                if (!notificationHashtags.isEmpty()) {
                    for (Map.Entry<String, String> item : notificationHashtags.entrySet())
                        notifyBrokersForHashTags(item.getKey(), item.getValue());
                }
                /**KOSTAS-END*/

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

                /**KOSTAS-START*/
                HashMap<String, String> notificationHashtags = channel.removeVideoFile(video);
                if (!notificationHashtags.isEmpty()) {
                    for (Map.Entry<String, String> item : notificationHashtags.entrySet())
                        notifyBrokersForHashTags(item.getKey(), item.getValue());
                }
                /**KOSTAS-END*/
            } else if (choice.equals("0")) {
                end = 1;
            }
        } while (end == 0);
    }
}
