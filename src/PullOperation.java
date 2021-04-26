import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class PullOperation {

    private HashMap<ChannelKey, String> hashtagVideoList;
    private CountDownLatch latch;

    PullOperation() {
        hashtagVideoList = null;
        latch = null;
    }

    public HashMap<ChannelKey, String> pullHashtags(String channel_or_hashtag, ArrayList<SocketAddress> addresses) {

        hashtagVideoList = new HashMap<>();

        //Check if this is channel name or hashtag
        try {
            //We use countdown latch and executor service to control threads when they finish
            //in order to continue our calling process only when all threads are finished
            int n_addresses = addresses.size();
            latch = new CountDownLatch(n_addresses);
            ExecutorService executorService = Executors.newFixedThreadPool(n_addresses);
            IntStream.range(0, n_addresses).forEach(index -> executorService.execute(
                    new PullThread(channel_or_hashtag, addresses.get(index))
            ));
            executorService.shutdown();

            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return hashtagVideoList;

    }

    public HashMap<Integer, String> pullChannel(SocketAddress publisherAddress) {

        String[] ipPort;
        InetAddress publisher_ip;
        int publisher_port;
        Socket pullSocket;
        ObjectOutputStream objectOutputStream;
        ObjectInputStream objectInputStream;
        HashMap<Integer, String> channelVideoList = null;

        try {
            //Split ip and port from address
            ipPort = publisherAddress.toString().split(":");
            publisher_ip = InetAddress.getByName(ipPort[0].substring(1));

            //Make connection with client
            pullSocket = new Socket(publisher_ip, 4900);
            objectInputStream = new ObjectInputStream(pullSocket.getInputStream());
            objectOutputStream = new ObjectOutputStream(pullSocket.getOutputStream());

            //Give option code
            objectOutputStream.writeObject(1);
            objectOutputStream.flush();

            //Give operation
            objectOutputStream.writeObject("CHANNEL");
            objectOutputStream.flush();

            //Store channel videos
            channelVideoList = (HashMap<Integer, String>) objectInputStream.readObject();

            //Close connections
            objectInputStream.close();
            objectOutputStream.close();
            pullSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return channelVideoList;
    }

    /** Thread-safe update of hashtag video list. */
    public synchronized void updateHashtagVideoList(HashMap<ChannelKey, String> list) {
        hashtagVideoList.putAll(list);
    }


    class PullThread extends Thread{

        private final String hashtag;
        public SocketAddress address;
        public InetAddress publisher_ip;
        public int publisher_port;

        /** Constructor */
        public PullThread(String hashtag, SocketAddress address) {
            this.hashtag = hashtag;
            this.address = address;
            try {
                String[] ipPort = address.toString().split(":");
                publisher_ip = InetAddress.getByName(ipPort[0].substring(1));
                publisher_port = Integer.parseInt(ipPort[1]);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        /** Run thread */
        @Override
        public void run() {

            try {

                //Make connection with client
                Socket pullSocket = new Socket(publisher_ip, 4900);
                ObjectInputStream objectInputStream = new ObjectInputStream(pullSocket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(pullSocket.getOutputStream());

                //Give option code
                objectOutputStream.writeObject(1);
                objectOutputStream.flush();

                //Give operation
                objectOutputStream.writeObject(hashtag);
                objectOutputStream.flush();

                //Receive video List
                HashMap<ChannelKey, String> channelVideoList = (HashMap<ChannelKey, String>) objectInputStream.readObject();
                System.out.println(channelVideoList);

                //Concatenate with larger list
                //PROBLEM : LOCAL VARIABLES ARE THREAD SAFE, SO I CANNOT ACCESS THEM INSIDE THREAD
                //(UNLESS THEY ARE FINAL, IN WHICH CASE I CANNOT UPDATE THEM) !!
                updateHashtagVideoList(channelVideoList);

                //Latch countdown
                latch.countDown();
                System.out.println(latch);


            } catch (IOException | ClassNotFoundException ioException) {
                ioException.printStackTrace();
            }

        }
    }

}
