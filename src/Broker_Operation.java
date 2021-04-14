import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;

public class Broker_Operation extends Thread{

    Socket socket;
    int thread_num;
    ObjectOutputStream objectOutputStream ;
    ObjectInputStream objectInputStream;

    Broker_Operation(Socket socket, int thread_num) {
        this.socket = socket;
        this.thread_num = thread_num;
        setName("Thread " + thread_num);

        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        while (true) {

        }

    }
}
