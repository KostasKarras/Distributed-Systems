package com.example.uni_tok;

import java.io.Serializable;
import java.util.ArrayList;

public class VideoInformation implements Serializable {
    private ChannelKey ck;
    private String videoTitle;
    private ArrayList<String> associatedHashtags;

    public VideoInformation(ChannelKey ck, String videoTitle, ArrayList associatedHashtags) {
        this.ck = ck;
        this.videoTitle = videoTitle;
        this.associatedHashtags = associatedHashtags;
    }

    public ChannelKey getChannelKey() {
        return ck;
    }

    public int getVideoID(){
        return ck.getVideoID();
    }

    public String getChannelName(){
        return ck.getChannelName();
    }

    public String getTitle(){
        return videoTitle;
    }

    public ArrayList<String> getHashtags(){
        return associatedHashtags;
    }
}