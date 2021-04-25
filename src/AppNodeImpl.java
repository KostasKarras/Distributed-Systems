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

    //CHANGE
    private static TreeMap<Integer, SocketAddress> brokerHashes = new TreeMap<>();
    //

    public static void main(String[] args) {
        new AppNodeImpl().initialize(4321);
    }

    @Override
    public void initialize(int port) {


        channel = new Channel("USER");

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
    public void notifyBrokersForHashTags(String hashtag) {//find apropriate broker by using hashtopic first.
        /**KOSTAS-START*/
        //klhsh sth hashtopic -> SocketAddress
        //syndesi sto socketAddress
        connect();
        try {
            objectOutputStream.writeObject(7);

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

    public void runUser() {
        //BUILD INTERFACE
        Scanner in = new Scanner(System.in);
        Scanner in2 = new Scanner(System.in);
        int end = 0;
        String choice;
        do {
            System.out.println("===== Menu =====");
            //Consumer Methods
            System.out.println("1. Register User");
            System.out.println("2. Get Topic Video List");
            System.out.println("3. Play Data");
            //Publisher Methods
            System.out.println("4. Add Hashtag to a Video");// Need to update multiple assosiated hashtags in many locations and for notification to brokers
            System.out.println("5. Remove Hashtag from a Video");
            System.out.println("6. Upload Video");
            System.out.println("7. Delete Video");
            System.out.println("0. Exit");
            //System.out.println("List with the brokers: " + this.getBrokerList());
            choice = in.nextLine();
            if (choice.equals("1")) {

            } else if (choice.equals("2")) {

            } else if (choice.equals("3")) {

            } else if (choice.equals("4")) {
                String video;
                boolean flag = true;
                boolean flag2 = true;
                int videoID = 0;
                do {
                    System.out.print("Name the video, that you want to add the hashtag: ");
                    video = in.nextLine();
                    if (channel.getChannelVideoNames().containsValue(video)) {
                        videoID = channel.getKeyFromValue(channel.getChannelVideoNames(), video);
                        flag = false;
                    }
                    if (flag) {
                        System.out.println("The video doesn't exists. Try again!");
                        System.out.print("You want to search for another video?(y/n)");
                        String answer = in2.next();
                        if (answer.equals("n"))
                            flag2 = false;
                    }
                }while(flag & flag2);

                boolean flag3 = true;
                do {
                    if (flag2) {
                        System.out.print("Give me the hashtag that you want to insert: ");
                        String hashtag = in.nextLine();
                        boolean exists = false;
                        boolean existsInPublisher = false;
                        if (channel.getVideoFile_byID(videoID).getAssociatedHashtags().contains(hashtag))
                            exists = true;
                        else {
                            if (channel.getHashtagsPublished().contains(hashtag))
                                existsInPublisher = true;
                        }
                        if (exists)
                            System.out.println("Hashtag already exists in the video.");
                        else {
                            channel.getVideoFile_byID(videoID).addAssociatedHashTags(hashtag);
                            if (!existsInPublisher) {
                                channel.addHashTag(hashtag);
                                ArrayList<VideoFile> videos = new ArrayList<>();
                                videos.add(channel.getVideoFile_byID(videoID));
                                channel.getHashtagVideoFilesMap().put(hashtag, videos);
                                notifyBrokersForHashTags(hashtag);
                            }
                        }
                        System.out.print("Do you want to add another one hashtag in the same video?(y/n)");
                        String answer = in.nextLine();
                        if (answer.equals("n"))
                            flag3 = false;
                    }
                }while (flag3);
            } else if (choice.equals("5")) {
                String video;
                boolean flag = true;
                boolean flag2 = true;
                int videoID = 0;
                do {
                    System.out.print("Name the video, that you want to add the hashtag: ");
                    video = in.nextLine();
                    if (channel.getChannelVideoNames().containsValue(video)) {
                        videoID = channel.getKeyFromValue(channel.getChannelVideoNames(), video);
                        flag = false;
                    }
                    if (flag) {
                        System.out.println("The video doesn't exists. Try again!");
                        System.out.print("You want to search for another video?(y/n)");
                        String answer = in2.next();
                        if (answer.equals("n"))
                            flag2 = false;
                    }
                }while(flag & flag2);

                boolean flag3 = true;
                do {
                    if (flag2) {
                        System.out.print("Give me the hashtag that you want to delete: ");
                        String hashtag = in.nextLine();
                        boolean exists = false;
                        boolean existsInAnotherVideo = false;
                        if (channel.getVideoFile_byID(videoID).getAssociatedHashtags().contains(hashtag))
                            exists = true;
                        if (channel.getVideoFiles_byHashtag(hashtag).size() > 1)
                            existsInAnotherVideo = true;
                        if (exists) {
                            channel.getVideoFile_byID(videoID).getAssociatedHashtags().remove(hashtag);
                            if (!existsInAnotherVideo) {
                                channel.getHashtagVideoFilesMap().remove(videoID);
                                channel.getHashtagsPublished().remove(hashtag);
                                notifyBrokersForHashTags(hashtag);//another argument in notifyBrokersForHashTags to know if we add or delete the hashtag
                            }
                        } else {
                            System.out.println("Hashtag doesn't exists in the video.");
                        }
                        System.out.print("Do you want to remove another one hashtag in the same video?(y/n)");
                        String answer = in.nextLine();
                        if (answer.equals("n"))
                            flag3 = false;
                    }
                }while (flag3);
            } else if (choice.equals("6")) {
                System.out.print("Give me the path of the file that you want to upload: ");
                String filepath = in.nextLine();
                System.out.print("Give me the name of the video: ");
                String name = in.nextLine();
                boolean flag;
                ArrayList<String> associatedHashtags = new ArrayList<>();
                do {
                    System.out.print("Do you want to add a hashtag?(y/n)");
                    String choice2 = in.nextLine();
                    if (choice2.equals("y")) {
                        flag = true;
                        System.out.print("Give me the hashtag: ");
                        String hashtag = in.nextLine();
                        boolean exists = false;
                        if (associatedHashtags.contains(hashtag))
                            System.out.println("Hashtag already exists.");
                        else
                            associatedHashtags.add(hashtag);
                    }
                    else {
                        flag = false;
                    }
                }while(flag);
                if (channel.getChannelVideoNames().containsValue(name))
                    System.out.println("Video already exists!");
                else {
                    VideoFile videoFile = new VideoFile(filepath, associatedHashtags, name);
                    channel.addVideoFile(videoFile);
                }
            } else if (choice.equals("7")){
                System.out.print("Give me the name of the file that you want to delete: ");
                String videoName = in.nextLine();
                if (channel.getChannelVideoNames().containsValue(videoName)){
                    int videoID = channel.getKeyFromValue(channel.getChannelVideoNames(), videoName);
                    channel.removeVideoFile(channel.getVideoFile_byID(videoID));
                    System.out.println("Video deleted Successfully!");
                }
                else
                    System.out.println("Video not found!");
            } else if (choice.equals("0")) {
                end = 1;
            }
        } while (end == 0);
    }
}
