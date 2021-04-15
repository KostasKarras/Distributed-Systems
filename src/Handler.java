import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Handler extends Thread {

    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    byte[] b = new byte[4096];
    Socket socket;

    public Handler(Socket socket) {
        this.socket = socket;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String message = null;
        try {
            byte[] chunk;
            ArrayList<byte[]> chunks = new ArrayList<byte[]>();

            int size = (int) objectInputStream.readObject();
            System.out.println("Size of the Arraylist is: " + size);

            for (int i = 0;i < size;i++){
                chunk = new byte[4096];
                chunk = objectInputStream.readAllBytes();
                chunks.add(chunk);
                System.out.println(this.socket.getInetAddress().getHostAddress() + ">" + chunk);
            }

            System.out.println("My Arraylist size: " + chunks.size());

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
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                objectInputStream.close();
                objectOutputStream.close();
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
