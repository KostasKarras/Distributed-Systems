//ADD \n BEFORE PRINT LINES IN EVERY SYSTEM.OUT.PRINTLN
//INITIALIZATION
//INITIALIZE UPLOADED VIDEOS AND FETCHED VIDEOS***

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class AppNodeImpl implements Publisher, Consumer{

    private static Socket requestSocket;
    private static ObjectOutputStream objectOutputStream;
    private static ObjectInputStream objectInputStream;

    private static Channel channel;

    private static TreeMap<Integer, SocketAddress> brokerHashes = new TreeMap<>();
    private static SocketAddress channelBroker;//NOT USED

    private static ServerSocket serverSocket;
    private static InetAddress multicastIP;
    private static int multicastPort;

    private static SocketAddress hear_address;

    public static void main(String[] args) {

        new AppNodeImpl().initialize(4950);
    }

    @Override
    public void initialize(int port) {

        //FIRST CONNECTION
        try {

            serverSocket = new ServerSocket(port, 60, InetAddress.getLocalHost());

            multicastIP = InetAddress.getByName("228.0.0.0");
            multicastPort = 5000;

            System.out.println("Welcome !");


            //CONNECT TO RANDOM BROKER TO RECEIVE BROKER HASHES
            //getBrokerMap();

            connect();
            objectOutputStream.writeObject(0);
            objectOutputStream.flush();

            brokerHashes = (TreeMap<Integer, SocketAddress>) objectInputStream.readObject();

            disconnect();

            boolean unique;

            while (true) {
                //CHANNEL NAME
                Scanner input = new Scanner(System.in);
                System.out.println("Channel name : ");
                String name = input.nextLine();
                channel = new Channel(name);

                //CONNECT TO APPROPRIATE BROKER
                channelBroker = hashTopic(channel.getChannelName());
                connect(channelBroker);

                //SEND OPTION 4 FOR INITIALIZATION
                objectOutputStream.writeObject(4);
                objectOutputStream.flush();

                //SEND CHANNEL NAME
                objectOutputStream.writeObject(channel.getChannelName());
                objectOutputStream.flush();

                //SEND SOCKET ADDRESS FOR CONNECTIONS
                //SocketAddress temp = new InetSocketAddress(InetAddress.getLocalHost(), RequestHandler.port);
                String string_socket = serverSocket.getLocalSocketAddress().toString().split("/")[1];
                String[] array = string_socket.split(":");
                InetAddress hear_ip = InetAddress.getByName(array[0]);
                int hear_port = Integer.parseInt(array[1]);
                hear_address = new InetSocketAddress(hear_ip, hear_port);
                System.out.println(hear_address);
                objectOutputStream.writeObject(hear_address);
                objectOutputStream.flush();

                //GET RESPONSE IF CHANNEL NAME IS UNIQUE
                unique = (boolean) objectInputStream.readObject();
                if (unique) {
                    break;
                }
                System.out.println("This channel name already exists. Pick another.\n");

            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }

        new RequestHandler(serverSocket).start();

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

    @Override
    public void notifyBrokersForHashTags(String hashtag, String action) {
        SocketAddress socketAddress = hashTopic(hashtag);
        connect(socketAddress);
        try {
            objectOutputStream.writeObject(7);
            objectOutputStream.flush();

            objectOutputStream.writeObject(hashtag);
            objectOutputStream.flush();

            objectOutputStream.writeObject(action);
            objectOutputStream.flush();

            objectOutputStream.writeObject(hear_address);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    public void notifyBrokersForChanges(ChannelKey channelKey, ArrayList<String> hashtags, String title, boolean action) {

        if (!hashtags.isEmpty()) {
            for (String hashtag : hashtags) {
                SocketAddress socketAddress = hashTopic(hashtag);
                connect(socketAddress);
                try {
                    objectOutputStream.writeObject(8);
                    objectOutputStream.flush();

                    objectOutputStream.writeObject("hashtag");
                    objectOutputStream.flush();

                    objectOutputStream.writeObject(hashtag);
                    objectOutputStream.flush();

                    objectOutputStream.writeObject(channelKey);
                    objectOutputStream.flush();

                    objectOutputStream.writeObject(title);
                        objectOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    disconnect();
                }
            }
        }

        if (action) {
            SocketAddress socketAddress = hashTopic(channelKey.getChannelName());
            connect(socketAddress);
            try {
                objectOutputStream.writeObject(8);
                objectOutputStream.flush();

                objectOutputStream.writeObject("channel");
                objectOutputStream.flush();

                objectOutputStream.writeObject(channelKey);
                objectOutputStream.flush();

                objectOutputStream.writeObject(title);
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }
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

    public TreeMap<Integer, SocketAddress> getBrokerMap() {
        /**DIMITRIS*/

        connect();
        try {
            objectOutputStream.writeObject(0);
            brokerHashes = (TreeMap<Integer, SocketAddress>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        disconnect();
        return brokerHashes;

        /*
        System.out.println("I am in here");

        try {
            serverSocket = new ServerSocket(4950, 60, InetAddress.getLocalHost());
            updateNodes();
            serverSocket.setSoTimeout(2000);
            try {
                Socket connectionSocket = serverSocket.accept();
                ObjectInputStream objectInputStream = new ObjectInputStream(connectionSocket.getInputStream());
                int option = (int) objectInputStream.readObject();
                brokerHashes = (TreeMap<Integer, SocketAddress>) objectInputStream.readObject();
            } catch (SocketTimeoutException ste) {
                System.out.println("Can't connect to a server. Terminating....");
                System.exit(-1);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            serverSocket.setSoTimeout(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
        */

    }

    private void connect(SocketAddress socketAddress) {
        try {
            requestSocket = new Socket();
            requestSocket.connect(socketAddress);
            objectOutputStream = new ObjectOutputStream(requestSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(requestSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect() {

        try {
            requestSocket = new Socket(InetAddress.getByName("172.17.0.2"), 4321);
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
    }

    @Override
    public void updateNodes() throws IOException {

        System.out.println("In update Nodes");

        MulticastSocket socket = new MulticastSocket(multicastPort);
        socket.joinGroup(multicastIP);

        //SEND % AND SOCKET ADDRESS TO RECEIVE BROKERHASHES
        String appNodeChar = "%";
        String address = serverSocket.getLocalSocketAddress().toString();
        String appNodeChar_address = appNodeChar + "," + address;
        byte[] buffer = appNodeChar_address.getBytes();

        //MAKE PACKET AND SEND IT
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastIP, multicastPort);
        socket.send(packet);

        try {
            TimeUnit.SECONDS.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Packet sent");

        socket.leaveGroup(multicastIP);

        //CLOSE SOCKET
        socket.close();

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

    public HashMap<ChannelKey, String> getChannelVideoMap() {
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

        public RequestHandler(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public void run() {

            try {
               //serverSocket = new ServerSocket(port, 60, InetAddress.getLocalHost());

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
                        HashMap<ChannelKey, String> videoList = getChannelVideoMap();
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
                } else if (option == 3) {
                    String notificationMessage = (String) objectInputStream.readObject();
                    System.out.println(notificationMessage);
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
            System.out.println("\n===== Menu =====");
            //Consumer Methods
            System.out.println("1. Register User");
            System.out.println("2. Get Topic Video List");
            System.out.println("3. Play Data");
            //Publisher Methods
            System.out.println("4. Add Hashtags to a Video");
            System.out.println("5. Remove Hashtags from a Video");
            System.out.println("6. Upload Video");
            System.out.println("7. Delete Video");
            System.out.println("8. Check Profile");
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

                    objectOutputStream.writeObject(channel.getChannelName());
                    objectOutputStream.flush();

                    objectOutputStream.writeObject(topic);
                    objectOutputStream.flush();
                    String response = (String) objectInputStream.readObject();
                    System.out.println(response);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

            } else if (choice.equals("2")) {

                //Give hashtag
                System.out.print("Please give the hashtag or the channel that you want to search for: ");
                String channel_or_hashtag = in.nextLine();

                //Get right broker
                SocketAddress socketAddress = hashTopic(channel_or_hashtag);

                //Connect to that broker
                connect(socketAddress);

                HashMap<ChannelKey, String> videoList = new HashMap<>();

                try {
                    //Write option
                    objectOutputStream.writeObject(2);
                    objectOutputStream.flush();

                    //Write channel name or hashtag
                    objectOutputStream.writeObject(channel_or_hashtag);
                    objectOutputStream.flush();

                    /**CHANGE*/
                    //Write this user's channel name
                    objectOutputStream.writeObject(channel.getChannelName());
                    objectOutputStream.flush();
                    /**END CHANGE*/

                    //Read videoList
                    videoList = (HashMap<ChannelKey, String>) objectInputStream.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    disconnect();
                }

                boolean wantVideo = true;
                if (videoList.isEmpty()) {
                    System.out.println("No videos\n");
                    wantVideo = false;
                }
                //CHOOSE SOME VIDEO OR GO BACK
                File nf = null;
                Scanner in2 = new Scanner(System.in);
                while (wantVideo) {
                    //System.out.println(videoList);
                    //MICHAEL
                    for (Map.Entry<ChannelKey, String> item : videoList.entrySet()) {
                        System.out.println("Channel Name : " + item.getKey().getChannelName() + "     Video ID : "
                                + item.getKey().getVideoID() + "    Video Name : " +item.getValue());
                    }
                    //
                    System.out.print("Do you want to see a video from these? (y/n)");
                    String answer = in.nextLine();

                    if (answer.equals("y")) {
                        try {
                            System.out.print("Give the Channel Name that you want to play: ");
                            String channelName = in2.nextLine();

                            System.out.print("Give the video ID that you want to play: ");
                            int videoID = in2.nextInt();

                            ChannelKey key = new ChannelKey(channelName, videoID);

                            //CONNECTING TO BROKER RESPONSIBLE FOR CHANNEL, THAT HAS THE VIDEO WE ASKED FOR
                            SocketAddress brokerAddress = hashTopic(channelName);
                            connect(brokerAddress);

                            objectOutputStream.writeObject(3);
                            objectOutputStream.flush();

                            objectOutputStream.writeObject(key);
                            objectOutputStream.flush();

                            //RECEIVE VIDEO FILE CHUNKS
                            byte[] chunk;
                            ArrayList<byte[]> chunks = new ArrayList<>();
                            int size = (int) objectInputStream.readObject();

                            if (size == 0) {
                                System.out.println("CHANNEL HAS NO VIDEO WITH THIS ID...");
                            }
                            //REBUILD CHUNKS FOR TESTING
                            else {
                                for (int i = 0; i < size; i++) {
                                    chunk = new byte[4096];
                                    chunk = objectInputStream.readAllBytes();
                                    chunks.add(chunk);
                                }
                                try {
                                    nf = new File("Fetched Videos\\" + channel.getChannelName() + "_"
                                            + channelName + "_" + videoID + ".mp4");
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
                                } finally {
                                    disconnect();
                                }
                            }
                        }catch(IOException | ClassNotFoundException e){
                            e.printStackTrace();
                        }
                    } else wantVideo = false;
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

                HashMap<String, String> notificationHashtags = channel.updateVideoFile(video, hashtags, "ADD");
                if (!notificationHashtags.isEmpty()) {
                    for (Map.Entry<String, String> item : notificationHashtags.entrySet())
                        notifyBrokersForHashTags(item.getKey(), item.getValue());
                }

                ChannelKey channelKey = new ChannelKey(channel.getChannelName(), video.getVideoID());
                notifyBrokersForChanges(channelKey, hashtags, video.getVideoName(), false);

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

                HashMap<String, String> notificationHashtags = channel.updateVideoFile(video, hashtags, "REMOVE");
                if (!notificationHashtags.isEmpty()) {
                    for (Map.Entry<String, String> item : notificationHashtags.entrySet())
                        notifyBrokersForHashTags(item.getKey(), item.getValue());
                }
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

                HashMap<String, String> notificationHashtags = channel.addVideoFile(video);

                //MICHAEL
                //CREATED A FOLDER TO STORE UPLOADED VIDEOS
                try {
                    Path source = Paths.get(filepath);
                    Path target = Paths.get("Uploaded Videos\\" + videoTitle + ".mp4");
                    Files.copy(source, target);
                } catch (IOException e) {
                    if (e instanceof FileAlreadyExistsException) {
                        System.out.println("There is already a video with that name. Upload cancelled...\n");
                    }
                    channel.removeVideoFile(video);
                }
                //

                if (!notificationHashtags.isEmpty()) {
                    for (Map.Entry<String, String> item : notificationHashtags.entrySet())
                        notifyBrokersForHashTags(item.getKey(), item.getValue());
                }

                ChannelKey channelKey = new ChannelKey(channel.getChannelName(), video.getVideoID());
                notifyBrokersForChanges(channelKey, associatedHashtags, videoTitle, true);

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

                HashMap<String, String> notificationHashtags = channel.removeVideoFile(video);

                //MICHAEL
                //DELETE VIDEO FROM UPLOADED VIDEOS
                try {
                    Path file = Paths.get("Uploaded Videos\\" + video.getVideoName() + ".mp4");
                    Files.delete(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //

                if (!notificationHashtags.isEmpty()) {
                    for (Map.Entry<String, String> item : notificationHashtags.entrySet())
                        notifyBrokersForHashTags(item.getKey(), item.getValue());
                }

            }else if (choice.equals("8")) { //CHECK PROFILE
                System.out.println(channel);
            }else if (choice.equals("0")) {
                end = 1;
            }
        } while (end == 0);
        System.exit(0);
    }
}
