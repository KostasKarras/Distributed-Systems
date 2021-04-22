import java.io.*;
import java.lang.ref.Reference;
import java.util.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class AppNodeImpl implements Publisher, Consumer{

    private static Socket requestSocket;
    private static ObjectOutputStream objectOutputStream; // MAY DELETE LATER or PASS THEM TO THE CONSUMER HANDLER
    private static ObjectInputStream objectInputStream;  // SAME
    private static ChannelName channel;
    private static int currentThreads = 1;

    public static void main(String[] args) {
        new AppNodeImpl().initialize(4321);
    }

    @Override
    public void initialize(int port) {


        channel = new ChannelName("USER");

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
    /**KOSTAS-START*/
    public void notifyBrokersForHashTags(String hashtag) {
        connect();
        try {
            objectOutputStream.writeObject(7);

            objectOutputStream.writeObject(hashtag);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            disconnect();
        }
    }
    /**KOSTAS-END*/

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

//    @Override
//    public void initialize(int port) {
//
//        channel = new ChannelName("user");
//
//        ArrayList<String> videoHashtags = new ArrayList<>();
//        videoHashtags.add("First File");
//
//        VideoFile vf = new VideoFile("C:\\Users\\miked\\Videos\\Captures\\Numb (Official Video) - Linkin Park - YouTube - Google Chrome 2020-04-03 14-10-06.mp4", videoHashtags);
//        push("#TIPOTES", vf);
//
//    }

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

    //CHANGES HAVE BEEN MADE
    class RequestHandler extends Thread {

        private static final int port = 9876;
        private int current_threads = 1;
        private ServerSocket serverSocket;

        public void run() {
            Socket connectionSocket = null;
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
                } catch (IOException e) {
                    e.printStackTrace();
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
            System.out.println("4. Add Hashtag to a Video");
            System.out.println("5. Remove Hashtag from a Video");
            System.out.println("6. Upload Video");
            System.out.println("7. Delete Video");
            System.out.println("0. Exit");
            choice = in.nextLine();
            if (choice.equals("1")) {

            } else if (choice.equals("2")) {

            } else if (choice.equals("3")) {

            } else if (choice.equals("4")) {
                /**KOSTAS-START*/
                String video;
                boolean flag = true;
                HashMap<Integer, VideoFile> channelsVideos;
                do {
                    System.out.print("Name the video, that you want to add the hashtag: ");
                    video = in.nextLine();
                    channelsVideos = channel.getChannelVideos();
                    for (Map.Entry<Integer, VideoFile> item : channelsVideos.entrySet()){
                        if (item.getValue().getVideoName().equals(video))
                            flag = false;
                    }
                    if (flag) {
                        System.out.println("The video doesn't exists. Try again!");
                        System.out.print("You want to search for another video?(y/n)");
                        String answer = in2.next();
                        if (answer.equals("n"))
                            flag = false;
                    }
                }while(flag);

                do {
                    if (!flag) {
                        System.out.print("Give me the hashtag that you want to insert: ");
                        String hashtag = in.nextLine();
                        boolean exists = false;
                        for (Map.Entry<Integer, VideoFile> item : channelsVideos.entrySet()) {
                            if (item.getValue().getVideoName().equals(video)) {
                                for (String videoHashtag : item.getValue().getAssociatedHashtags()) {
                                    if (videoHashtag.equals(hashtag))
                                        exists = true;
                                }
                                if (exists)
                                    System.out.println("Hashtag already exists.");
                                else {
                                    ArrayList<String> temp = new ArrayList<String>(item.getValue().getAssociatedHashtags());
                                    temp.add(hashtag);
                                    item.getValue().getAssociatedHashtags().clear();
                                    for (String tempHashtag : temp)
                                        item.getValue().addAssociatedHashTags(tempHashtag);
                                    notifyBrokersForHashTags(hashtag);
                                }
                            }
                        }
                    } else {
                        continue;
                    }
                    System.out.print("Do you want to add another one hashtag in the same video?(y/n)");
                    String answer = in.nextLine();
                    if (answer.equals("n"))
                        break;
                }while (true);
                /**KOSTAS-END*/
                // if (exo anebasei video me ayto to hashtag)
                // pass
                // else
                // notify
            } else if (choice.equals("5")) {
                String video;
                boolean flag = true;
                do {
                    System.out.print("Name the video, that you want to remove the hashtag: ");
                    video = in.nextLine();
                    for (Map.Entry<Integer, VideoFile> item : this.channel.getChannelVideos().entrySet()){
                        if (item.getValue().getVideoName().equals(video))
                            flag = false;
                    }
                    if (flag) {
                        System.out.println("The video doesn't exists. Try again!");
                        System.out.print("You want to search for another video?(y/n)");
                        String answer = in2.next();
                        if (answer.equals("n"))
                            flag = false;
                    }
                }while(flag);

                do {
                    if (!flag) {
                        System.out.print("Give me the hashtag that you want to remove: ");
                        String hashtag = in.nextLine();
                        boolean exists = false;
                        for (Map.Entry<Integer, VideoFile> item : this.channel.getChannelVideos().entrySet()) {
                            if (item.getValue().getVideoName().equals(video)) {
                                for (String videoHashtag : item.getValue().getAssociatedHashtags()) {
                                    if (videoHashtag.equals(hashtag))
                                        exists = true;
                                }
                                if (exists){
                                    ArrayList<String> temp = new ArrayList<String>(item.getValue().getAssociatedHashtags());
                                    temp.remove(hashtag);
                                    item.getValue().getAssociatedHashtags().clear();
                                    for (String tempHashtag : temp)
                                        item.getValue().addAssociatedHashTags(tempHashtag);//may notify the brokers
                                    System.out.println("Hashtag '" + hashtag + "' removed.");
                                }
                                else
                                    System.out.println("Hashtag doesn't exists.");
                            }
                        }
                    } else {
                        continue;
                    }
                    System.out.print("Do you want to remove another one hashtag in the same video?(y/n)");
                    String answer = in.nextLine();
                    if (answer.equals("n"))
                        break;
                }while (true);
            } else if (choice.equals("6")) {
                System.out.print("Give me the path of the file that you want to upload: ");
                String filepath = in.nextLine();
                for (Map.Entry<Integer, VideoFile> item : this.channel.getChannelVideos().entrySet()) {
                    if (item.getValue().getVideoName().equals(filepath))
                        System.out.println("Video is already uploaded!");
                }
                boolean flag;
                ArrayList<String> associatedHashtags = new ArrayList<String>();
                do {
                    System.out.print("Do you want to add a hashtag?(y/n)");
                    String choice2 = in.nextLine();
                    if (choice2.equals("y")) {
                        flag = true;
                        System.out.print("Give me the hashtag: ");
                        String hashtag = in.nextLine();
                        boolean exists = false;
                        for (String hashtagIn : associatedHashtags){
                            if (hashtagIn.equals(hashtag)) {
                                System.out.println("Hashtag already exists.");
                                exists = true;
                            }
                        }
                        if (!exists)
                            associatedHashtags.add(hashtag);
                    }
                    else {
                        flag = false;
                    }
                }while(flag);
                VideoFile videoFile = new VideoFile(filepath, associatedHashtags);
                this.channel.addVideoFile(videoFile);
                for (String hashtag : associatedHashtags){
                    notifyBrokersForHashTags(hashtag);
                }
            } else if (choice.equals("7")){
                System.out.print("Give me the path of the file that you want to delete: ");
                String filepath = in.nextLine();
                HashMap<Integer, VideoFile> channelsVideos = channel.getChannelVideos();
                boolean flag = false;
                for (Map.Entry<Integer, VideoFile> item : channelsVideos.entrySet()) {
                    if (item.getValue().getVideoName().equals(filepath)) {
                        channelsVideos.remove(item.getKey());
                        flag = true;
                    }
                }
                if (flag)//may notify the brokers or when a broker asks for this video we sent notify for failure?
                    System.out.println("Video deleted from channel!");
                else
                    System.out.println("Video not found!");
            } else if (choice.equals("0")) {
                end = 1;
            }
        } while (end == 0);
    }
}
