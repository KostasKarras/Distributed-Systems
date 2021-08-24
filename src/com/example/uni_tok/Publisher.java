package com.example.uni_tok;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.util.ArrayList;

interface Publisher extends Node{

    public void addHashTag(VideoFile video);

    public void removeHashTag(VideoFile video);

    public SocketAddress hashTopic(String hashtopic);

    public void push(int id, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream);

    public void notifyBrokersForHashTags(String hashtag, String action);

    public ArrayList<byte[]> generateChunks(VideoFile video);
}
