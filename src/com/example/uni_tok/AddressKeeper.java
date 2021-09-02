package com.example.uni_tok;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.TreeMap;

public class AddressKeeper {

    private static ServerSocket serverSocket;
    private static TreeMap<Integer, SocketAddress> brokerHashes;

    public void init(){
        Socket connectionSocket = null;
        try {
            //I USE THIS SOCKET TO GET THE IP
            Socket socket = new Socket(InetAddress.getByName("google.com"), 80);
            String ip = socket.getLocalAddress().toString();
            socket.close();
            //to remove the slash symbol from the ip address I use the substring(1)
            System.out.println("ADDRESS KEEPER IP: " + ip.substring(1));
            serverSocket = new ServerSocket(4000, 60, InetAddress.getByName(ip.substring(1)));

            /**IMPORTANT
             * If I want to run the app from emulator
             * I must uncomment the lines 33-34 and
             * comment the lines 21-23 and 25-26 */

//            serverSocket = new ServerSocket(4000, 60, InetAddress.getLocalHost());
//            System.out.println("Address Keeper IP : " + InetAddress.getLocalHost().getHostAddress());

            while(true) {
                connectionSocket = serverSocket.accept();
                new Handler(connectionSocket).start();
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

    public synchronized void addBrokers(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream){
        SocketAddress socketAddress = null;
        try {
            socketAddress = (SocketAddress) objectInputStream.readObject();
            System.out.println("Socket address: " + socketAddress);
            int brokerHash = (int) objectInputStream.readObject();
            brokerHashes.put(brokerHash, socketAddress);
            System.out.println(brokerHashes);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    class Handler extends Thread {

        Socket socket;
        ObjectInputStream objectInputStream;
        ObjectOutputStream objectOutputStream;

        /**
         * Construct a Handler
         */
        Handler(Socket s) {
            socket = s;
            try {
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.flush();
                objectInputStream = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            try {
                int option = (int) objectInputStream.readObject();

                if (option == 1){
                    //Broker
                    addBrokers(objectInputStream, objectOutputStream);
                } else if (option == 2){
                    //AppNode
                    objectOutputStream.writeObject(brokerHashes);
                    objectOutputStream.flush();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                objectInputStream.close();
                objectOutputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        AddressKeeper addressKeeper = new AddressKeeper();
        brokerHashes = new TreeMap<>();
        addressKeeper.init();
    }
}
