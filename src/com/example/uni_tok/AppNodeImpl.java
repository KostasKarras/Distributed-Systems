package com.example.uni_tok;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.TreeMap;

public class AppNodeImpl implements Publisher, Consumer{

    private static Socket requestSocket;
    private static ObjectOutputStream objectOutputStream;
    private static ObjectInputStream objectInputStream;

    private static Channel channel;

    private static TreeMap<Integer, SocketAddress> brokerHashes = new TreeMap<>();
    private static SocketAddress channelBroker;

    private static ServerSocket serverSocket;
    private static InetAddress multicastIP;
    private static int multicastPort;

    private static SocketAddress hear_address;

    private static ArrayList<String> subscribedToChannels = new ArrayList<>();
    private static ArrayList<String> subscribedToHashtags = new ArrayList<>();

    public static void main(String[] args) {

        new AppNodeImpl().initialize(4960);
    }

    @Override
    public void initialize(int port) {

        //FIRST CONNECTION
        try {

            serverSocket = new ServerSocket(port, 60, InetAddress.getLocalHost());

//            multicastIP = InetAddress.getByName("228.0.0.0");
//            multicastPort = 5000;


            //GET BROKERS
            connect();
            try {
                objectOutputStream.writeObject(2);
                objectOutputStream.flush();

                brokerHashes = (TreeMap<Integer, SocketAddress>) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }

            System.out.println("Welcome !");


//            CONNECT TO RANDOM BROKER TO RECEIVE BROKER HASHES
//            getBrokerMap();

            boolean unique;

            while (true) {
                //CHANNEL NAME
                Scanner input = new Scanner(System.in);
                System.out.println("com.example.uni_tok.Channel name : ");
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
                String string_socket = serverSocket.getLocalSocketAddress().toString().split("/")[1];
                String[] array = string_socket.split(":");
                InetAddress hear_ip = InetAddress.getByName(array[0]);
                int hear_port = Integer.parseInt(array[1]);
                hear_address = new InetSocketAddress(hear_ip, hear_port);
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
    public void addHashTag(VideoFile video) {

        Scanner in3 = new Scanner(System.in);
        String hashtag;
        ArrayList<String> hashtags = new ArrayList<>();
        while (true) {
            System.out.print("Do you want to add a hashtag to this video? (y/n) ");
            String answer = in3.nextLine();
            if (answer.equals("n")) {
                break;
            }

            System.out.print("Please give the hashtag that you want to add: ");
            hashtag = in3.nextLine();

            if (!hashtags.contains(hashtag) && !video.getAssociatedHashtags().contains(hashtag)) {
                hashtags.add(hashtag);
            }
        }

        if (hashtags.isEmpty()) {
            System.out.println("No hashtags found to add.");
        } else {

            HashMap<String, String> notificationHashtags = channel.updateVideoFile(video, hashtags, "ADD");
            if (!notificationHashtags.isEmpty()) {
                for (Map.Entry<String, String> item : notificationHashtags.entrySet())
                    notifyBrokersForHashTags(item.getKey(), item.getValue());
            }

            ChannelKey channelKey = new ChannelKey(channel.getChannelName(), video.getVideoID());
            notifyBrokersForChanges(channelKey, hashtags, video.getVideoName(), video.getAssociatedHashtags(), false);
        }
    }

    @Override
    public void removeHashTag(VideoFile video) {

        Scanner in4 = new Scanner(System.in);
        String hashtag;
        ArrayList<String> hashtags = new ArrayList<>();
        while (true) {
            System.out.print("Do you want to remove a hashtag to this video? (y/n) ");
            String answer = in4.nextLine();
            if (answer.equals("n")) {
                break;
            }

            System.out.print("Please give the hashtag that you want to remove: ");
            hashtag = in4.nextLine();

            if (!hashtags.contains(hashtag) && video.getAssociatedHashtags().contains(hashtag)) {
                hashtags.add(hashtag);
            }
        }

        if (hashtags.isEmpty()) {
            System.out.println("No hashtags found to remove.");
        } else {

            HashMap<String, String> notificationHashtags = channel.updateVideoFile(video, hashtags, "REMOVE");
            if (!notificationHashtags.isEmpty()) {
                for (Map.Entry<String, String> item : notificationHashtags.entrySet())
                    notifyBrokersForHashTags(item.getKey(), item.getValue());
            }
        }
    }

//    @Override
//    public List<com.example.uni_tok.Broker> getBrokerList() {
//        return brokers;
//    }

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
        } finally {
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

//    @Override
//    public void notifyFailure(com.example.uni_tok.Broker broker) {
//
//    }

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

    public void notifyBrokersForChanges(ChannelKey channelKey, ArrayList<String> hashtags, String title, ArrayList<String> associatedHashtags, boolean action) {

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

                    objectOutputStream.writeObject(associatedHashtags);
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

                objectOutputStream.writeObject(associatedHashtags);
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

//    public TreeMap<Integer, SocketAddress> getBrokerMap() {
//
//        connect();
//        try {
//            objectOutputStream.writeObject(2);
//            objectOutputStream.flush();
//
//            brokerHashes = (TreeMap<Integer, SocketAddress>) objectInputStream.readObject();
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        } finally {
//            disconnect();
//        }
//        return brokerHashes;

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

//    }

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
            Scanner in5 = new Scanner(System.in);
            System.out.println("Give Address Keeper IP address : ");
            String inetAddress = in5.nextLine();
            requestSocket = new Socket(InetAddress.getByName(inetAddress), 4000);
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

//    @Override
//    public void updateNodes() throws IOException {
//
//        System.out.println("In update Nodes");
//
//        MulticastSocket socket = new MulticastSocket(multicastPort);
//        socket.joinGroup(multicastIP);
//
//        //SEND % AND SOCKET ADDRESS TO RECEIVE BROKERHASHES
//        String appNodeChar = "%";
//        String address = serverSocket.getLocalSocketAddress().toString();
//        String appNodeChar_address = appNodeChar + "," + address;
//        byte[] buffer = appNodeChar_address.getBytes();
//
//        //MAKE PACKET AND SEND IT
//        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastIP, multicastPort);
//        socket.send(packet);
//
//        try {
//            TimeUnit.SECONDS.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println("Packet sent");
//
//        socket.leaveGroup(multicastIP);
//
//        //CLOSE SOCKET
//        socket.close();
//
//    }

    @Override
    public void register(SocketAddress socketAddress, String topic) {

        connect(socketAddress);

        try {
            objectOutputStream.writeObject(1);
            objectOutputStream.flush();

            objectOutputStream.writeObject(channel.getChannelName());
            objectOutputStream.flush();

            objectOutputStream.writeObject(topic);
            objectOutputStream.flush();

            objectOutputStream.writeObject(hear_address);
            objectOutputStream.flush();

            String response = (String) objectInputStream.readObject();
            System.out.println(response);

            if (response.contains("successfully")) {
                if (topic.charAt(0) == '#') {
                    subscribedToHashtags.add(topic);
                } else {
                    subscribedToChannels.add(topic);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    @Override
    public void unregister(SocketAddress socketAddress, String topic) {

        try {
            connect(socketAddress);

            objectOutputStream.writeObject(9);
            objectOutputStream.flush();

            objectOutputStream.writeObject(topic);
            objectOutputStream.flush();

            objectOutputStream.writeObject(hear_address);
            objectOutputStream.flush();

            if (topic.charAt(0) == '#'){
                System.out.println("You unsubscribed from hashtag " + topic + " successfully.");
            } else {
                System.out.println("You unsubscribed from channel " + topic + " successfully.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    @Override
    public void playData(HashMap<ChannelKey, String> videoList) {

        File nf = null;
        Scanner in2 = new Scanner(System.in);

        try {
            System.out.print("Give the com.example.uni_tok.Channel Name that you want to play: ");
            String channelName = in2.nextLine();

            System.out.print("Give the video ID that you want to play: ");
            int videoID = in2.nextInt();

            ChannelKey key = new ChannelKey(channelName, videoID);

            if (!videoList.containsKey(key)){
                System.out.println("This combination of channel name and id doesn't exist.");
            } else {
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
            }
        } catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public HashMap<ChannelKey, String> getChannelVideoMap() {
        return channel.getChannelVideoNames();
    }

    public static HashMap<ChannelKey, ArrayList<String>> getChannelHashtagsMap() {
        return channel.getChannelAssociatedHashtags();
    }

    public HashMap<ChannelKey, String> getHashtagVideoMap(String hashtag) {
        return channel.getChannelVideoNamesByHashtag(hashtag);
    }


    //CHANGES HAVE BEEN MADE
    class RequestHandler extends Thread {

        public ServerSocket serverSocket;
        public Socket connectionSocket;

        public RequestHandler(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public void run() {

            try {
                while(true) {
                    connectionSocket = serverSocket.accept();
                    new ServeRequest(connectionSocket).start();
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
        private ObjectInputStream objectInputStream;
        private ObjectOutputStream objectOutputStream;

        ServeRequest(Socket s) {
            socket = s;
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
                        ArrayList<VideoInformation> videoList = new ArrayList<>();
                        for (ChannelKey ck : getChannelVideoMap().keySet()) {
                            VideoInformation vi = new VideoInformation(ck, getChannelVideoMap().get(ck), getChannelHashtagsMap().get(ck));
                            videoList.add(vi);
                        }
                        objectOutputStream.writeObject(videoList);
                    }
                    else {
                        ArrayList<VideoInformation> videoList = new ArrayList<>();
                        for (ChannelKey ck : getHashtagVideoMap(choice).keySet()) {
                            VideoInformation vi = new VideoInformation(ck, getChannelVideoMap().get(ck), getChannelHashtagsMap().get(ck));
                            videoList.add(vi);
                        }
                        objectOutputStream.writeObject(videoList);
                    }

                } else if (option == 2) { //Pull Video

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
                    VideoInformation vi = (VideoInformation) objectInputStream.readObject();

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
            //com.example.uni_tok.Consumer Methods
            System.out.println("1. Register User to hashtag or channel");
            System.out.println("2. Get Topic Video List");
            System.out.println("3. Unregister User from hashtag or channel");
            //com.example.uni_tok.Publisher Methods
            System.out.println("4. Add Hashtags to a Video");
            System.out.println("5. Remove Hashtags from a Video");
            System.out.println("6. Upload Video");
            System.out.println("7. Delete Video");
            System.out.println("8. Check Profile");
            System.out.println("0. Exit");
            choice = in.nextLine();
            if (choice.equals("1")) {

                String topic;
                System.out.print("Please select a topic (hashtag/channel) that you want to subscribe: ");
                topic = in.nextLine();
                SocketAddress socketAddress = hashTopic(topic);
                register(socketAddress, topic);

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

                    //Write this user's channel name
                    objectOutputStream.writeObject(channel.getChannelName());
                    objectOutputStream.flush();

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
                while (wantVideo) {
                    for (Map.Entry<ChannelKey, String> item : videoList.entrySet()) {
                        System.out.println("com.example.uni_tok.Channel Name : " + item.getKey().getChannelName() + "     Video ID : "
                                + item.getKey().getVideoID() + "    Video Name : " +item.getValue());
                    }

                    System.out.print("Do you want to see a video from these? (y/n)");
                    String answer = in.nextLine();

                    if (answer.equals("y")) {
                        playData(videoList);
                    } else wantVideo = false;
                }

            } else if (choice.equals("3")) {

                String topic;
                System.out.print("Please select 'channel' if you want to unsubscribe from a channel " +
                        "or 'hashtag' to unsubscribe from a hashtag: ");
                topic = in.nextLine();

                if (topic.equals("channel")){
                    boolean wantUnsubscribe = true;
                    Scanner in2 = new Scanner(System.in);
                    String answer = "";
                    while (wantUnsubscribe){
                        if (subscribedToChannels.isEmpty()){
                            if (answer.equals(""))
                                System.out.println("You aren't subscribed to any channel. Unsubscribe cancelled...");
                            answer = "n";
                        } else {
                            System.out.println("Channels that you are subscribed: ");
                            for (String channel : subscribedToChannels)
                                System.out.println(channel);
                            System.out.println("Do you want to unsubscribe from one of the above channels? (y/n)");
                            answer = in2.nextLine();
                        }

                        if (answer.equals("y")){
                            try {
                                System.out.println("Give me the channel name that you want to unsubscribe: ");
                                String channelName = in2.nextLine();

                                if (!subscribedToChannels.contains(channelName)) {
                                    System.out.println("You are not subscribed to channel " + channelName);
                                    continue;
                                }

                                subscribedToChannels.remove(channelName);

                                SocketAddress socketAddress1 = hashTopic(channelName);

                                unregister(socketAddress1, channelName);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            wantUnsubscribe = false;
                        }
                    }
                } else if (topic.equals("hashtag")){
                    boolean wantUnsubscribe = true;
                    Scanner in2 = new Scanner(System.in);
                    String answer = "";
                    while (wantUnsubscribe){
                        if (subscribedToHashtags.isEmpty()){
                            if (answer.equals(""))
                                System.out.println("You aren't subscribed to any hashtag. Unsubscribe cancelled...");
                            answer = "n";
                        } else {
                            System.out.println("Hashtags that you are subscribed: ");
                            for (String hashtag : subscribedToHashtags)
                                System.out.println(hashtag);
                            System.out.println("Do you want to unsubscribe from one of the above hashtags? (y/n)");
                            answer = in2.nextLine();
                        }

                        if (answer.equals("y")){
                            try {
                                System.out.println("Give me the hashtag that you want to unsubscribe: ");
                                String hashtag = in2.nextLine();

                                if (!subscribedToHashtags.contains(hashtag)) {
                                    System.out.println("You are not subscribed to hashtag " + hashtag);
                                    continue;
                                }

                                subscribedToHashtags.remove(hashtag);

                                SocketAddress socketAddress1 = hashTopic(hashtag);

                                unregister(socketAddress1, hashtag);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            wantUnsubscribe = false;
                        }
                    }
                } else {
                    System.out.println("You didn't choose 'channel' or 'hashtag'. Unsubscribe cancelled...");
                }

            } else if (choice.equals("4")) {

                int videoID;

                if (channel.getID_VideoFileMap().isEmpty()) {
                    System.out.println("The channel doesn't have any videos to add hashtags.");
                    continue;
                }

                System.out.println(channel.toString());

                System.out.print("Please give the videoID of the video you want to add a hashtag: ");
                videoID = Integer.parseInt(in.nextLine());

                VideoFile video = channel.getVideoFile_byID(videoID);

                addHashTag(video);

            } else if (choice.equals("5")) {

                int videoID;

                if (channel.getID_VideoFileMap().isEmpty()) {
                    System.out.println("The channel doesn't have any videos to remove hashtags.");
                    continue;
                }

                System.out.println(channel.toString());

                System.out.print("Please give the videoID of the video you want to remove a hashtag: ");
                videoID = Integer.parseInt(in.nextLine());

                VideoFile video = channel.getVideoFile_byID(videoID);

                removeHashTag(video);

            } else if (choice.equals("6")) {

//                JFileChooser chooser = new JFileChooser(){
//                    @Override
//                    protected JDialog createDialog(Component parent) throws HeadlessException {
//                        JDialog jDialog = super.createDialog(parent);
//                        jDialog.setAlwaysOnTop(true);
//                        return jDialog;
//                    }
//                };
//                FileNameExtensionFilter filter = new FileNameExtensionFilter(".mp4", "mp4");
//                chooser.setFileFilter(filter);
//                int returnVal = chooser.showOpenDialog(null);
//                if (returnVal == JFileChooser.APPROVE_OPTION) {
//                    System.out.println("You chose to upload this file: "+chooser.getSelectedFile().getAbsolutePath());
//
//                    ArrayList<String> associatedHashtags = new ArrayList<>();
//
//                    String filepath = chooser.getSelectedFile().getAbsolutePath();
//
//                    String videoTitle = chooser.getSelectedFile().getName().split("\\.")[0];
//
//                    String hashtag;
//                    while (true) {
//                        System.out.print("Do you want to add a hashtag to your video? (y/n) ");
//                        String answer = in.nextLine();
//                        if (answer.equals("n")) {
//                            break;
//                        }
//
//                        System.out.print("Please give a hashtag for the video: ");
//                        hashtag = in.nextLine();
//
//                        if (!associatedHashtags.contains(hashtag)) {
//                            associatedHashtags.add(hashtag);
//                        }
//                    }
//
//                    com.example.uni_tok.VideoFile video = new com.example.uni_tok.VideoFile(filepath, associatedHashtags, videoTitle);
//
//                    HashMap<String, String> notificationHashtags = channel.addVideoFile(video);
//
//                    boolean notExists = true;
//                    try {
//                        Path source = Paths.get(filepath);
//                        Path target = Paths.get("Uploaded Videos\\" + videoTitle + ".mp4");
//                        Files.copy(source, target);
//                    } catch (IOException e) {
//                        if (e instanceof FileAlreadyExistsException) {
//                            System.out.println("There is already a video with that name. Upload cancelled...\n");
//                        }
//                        notExists = false;
//                    }
//
//                    if (notExists) {
//                        if (!notificationHashtags.isEmpty()) {
//                            for (Map.Entry<String, String> item : notificationHashtags.entrySet())
//                                notifyBrokersForHashTags(item.getKey(), item.getValue());
//                        }
//
//                        com.example.uni_tok.ChannelKey channelKey = new com.example.uni_tok.ChannelKey(channel.getChannelName(), video.getVideoID());
//                        notifyBrokersForChanges(channelKey, associatedHashtags, videoTitle, true);
//                    } else {
//                        channel.removeVideoFile(video);
//                    }
//                } else {
//                    System.out.println("You didn't choose any file. Upload cancelled...");
//                }

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
                boolean notExists = true;
                try {
                    Path source = Paths.get(filepath);
                    Path target = Paths.get("Uploaded Videos\\" + videoTitle + ".mp4");
                    Files.copy(source, target);
                } catch (IOException e) {
                    if (e instanceof FileAlreadyExistsException) {
                        System.out.println("There is already a video with that name. Upload cancelled...\n");
                    }
                    e.printStackTrace();
                    notExists = false;
                }

                if (notExists) {
                    if (!notificationHashtags.isEmpty()) {
                        for (Map.Entry<String, String> item : notificationHashtags.entrySet())
                            notifyBrokersForHashTags(item.getKey(), item.getValue());
                    }

                    ChannelKey channelKey = new ChannelKey(channel.getChannelName(), video.getVideoID());
                    notifyBrokersForChanges(channelKey, associatedHashtags, videoTitle, associatedHashtags, true);
                } else {
                    channel.removeVideoFile(video);
                }
            }

            else if (choice.equals("7")){

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

                try {
                    Path file = Paths.get("Uploaded Videos\\" + video.getVideoName() + ".mp4");
                    Files.delete(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!notificationHashtags.isEmpty()) {
                    for (Map.Entry<String, String> item : notificationHashtags.entrySet())
                        notifyBrokersForHashTags(item.getKey(), item.getValue());
                }

            }else if (choice.equals("8")) {

                System.out.println(channel);

            }else if (choice.equals("0")) {

                end = 1;

            }
        } while (end == 0);

        System.exit(0);

    }
}