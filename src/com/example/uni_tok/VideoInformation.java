package com.example.uni_tok;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class VideoInformation implements Serializable, Comparable<VideoInformation> {
    private ChannelKey ck;
    private String videoTitle;
    private ArrayList<String> associatedHashtags;
    private Date date;
    private byte [] thumbnail;

    public VideoInformation(ChannelKey ck, String videoTitle, ArrayList associatedHashtags, byte [] thumbnail) {
        this.ck = ck;
        this.videoTitle = videoTitle;
        this.associatedHashtags = associatedHashtags;
        this.thumbnail = thumbnail;
        date = ck.getDate();
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

    public String getTitle(){ return videoTitle; }

    public ArrayList<String> getHashtags(){
        return associatedHashtags;
    }

    public Date getDate() {
        return date;
    }

    public byte[] getThumbnail() { return this.thumbnail; }

    public int compareTo(VideoInformation vi) {
        return this.getDate().compareTo(vi.getDate()) * (-1);
    }
}