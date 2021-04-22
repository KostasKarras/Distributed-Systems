import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Channel {

    private String channelName;
    private ArrayList<String> hashtagsPublished;
    private HashMap<String, ArrayList<VideoFile>> hashtagVideoFilesMap;
    private int counterVideoID;
    private HashMap<Integer, VideoFile> ID_VideoFileMap;

    /** Constructors */

    //For new users
    public Channel(String channelName) {
        this.channelName = channelName;
        hashtagsPublished = new ArrayList<>();
        hashtagVideoFilesMap = new HashMap<>();
        counterVideoID = 0;
        ID_VideoFileMap = new HashMap<>();
    }

    //To create existing channels
    public Channel(String channelName, ArrayList<String> hashtagsPublished, HashMap<String, ArrayList<VideoFile>>
            hashtagVideoFilesMap, int counterVideoID, HashMap<Integer, VideoFile> ID_VideoFileMap) {
        this.channelName = channelName;
        this.hashtagsPublished = hashtagsPublished;
        this.hashtagVideoFilesMap = hashtagVideoFilesMap;
        this.counterVideoID = counterVideoID;
        this.ID_VideoFileMap = ID_VideoFileMap;
    }

    public void addVideoFile(VideoFile video) {
        video.setVideoID(counterVideoID);
        ID_VideoFileMap.put(counterVideoID, video);
        counterVideoID++;

        ArrayList<String> hashtags = video.getAssociatedHashtags();
        for (String hashtag : hashtags) {
            if (hashtagsPublished.contains(hashtag)) {
                ArrayList<VideoFile> value = hashtagVideoFilesMap.get(hashtag);
                value.add(video);
                hashtagVideoFilesMap.put(hashtag, value);
            } else {
                ArrayList<VideoFile> value = new ArrayList<>();
                value.add(video);
                hashtagVideoFilesMap.put(hashtag, value);

                this.addHashTag(hashtag);
            }
        }
    }

    public void removeVideoFile(VideoFile video) {
        ID_VideoFileMap.remove(video.getVideoID());

        ArrayList<String> hashtags = video.getAssociatedHashtags();
        for (String hashtag : hashtags) {
            if (hashtagVideoFilesMap.get(hashtag).size() == 1) {
                hashtagVideoFilesMap.remove(hashtag);
            } else {
                ArrayList<VideoFile> value = hashtagVideoFilesMap.get(hashtag);
                value.remove(video);
                hashtagVideoFilesMap.put(hashtag, value);
            }
        }
    }

    /** Getters */

    public ArrayList<VideoFile> getVideoFiles_byHashtag(String hashtag) {
        return hashtagVideoFilesMap.get(hashtag);
    }

    public VideoFile getVideoFile_byID (int ID) {
        return ID_VideoFileMap.get(ID);
    }

    /** Setters */
    public void addHashTag(String hashtag) {
        hashtagsPublished.add(hashtag);
    }

    public void removeHashTag(String hashtag) {
        hashtagsPublished.remove(hashtag);
    }

    public HashMap<Integer, VideoFile> getChannelVideos(){
        return this.ID_VideoFileMap;
    }



}