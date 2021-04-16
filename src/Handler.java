import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Handler extends Thread {

    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    Socket socket;
    static int version = 1;

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

            message = (String) objectInputStream.readObject();
            if(message.equals("Hello! New Client here!"))
                System.out.println(socket.getInetAddress().getHostAddress() + ">New Client connected.");

            objectOutputStream.writeObject("Connection successful!");
            objectOutputStream.flush();

            int size = (int) objectInputStream.readObject();

            for (int i = 0;i < size;i++){
                chunk = new byte[4096];
                chunk = objectInputStream.readAllBytes();
                chunks.add(chunk);
            }

            try {
                File nf = new File("C:/Users/Kostas/Desktop/test" + version++ + ".mp4");
                for (byte[] ar : chunks) {
                    FileOutputStream fw = new FileOutputStream(nf, true);
                    try {
                        fw.write(ar);
                    } finally {
                        fw.close();
                    }
                }
                System.out.println(socket.getInetAddress().getHostAddress() + ">The file received Successfully!");
                message = (String) objectInputStream.readObject();
                if (message.equals("Bye"))
                    System.out.println(socket.getInetAddress().getHostAddress() + ">Client disconnected.");
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
