package com.example.uni_tok;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

interface Consumer extends Node{

    public void register(SocketAddress socketAddress, String topic);

    public void unregister(SocketAddress socketAddress, String topic);

    public void playData(ArrayList<VideoInformation> videoList);

}