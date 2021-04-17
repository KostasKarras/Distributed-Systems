import java.util.ArrayList;
import java.util.HashMap;

public class ChannelName {

    private String channelName;
    private ArrayList<String> hashtagsPublished;
    private HashMap<String, ArrayList<VideoFile>> userVideoFilesMap;

    /** Constructors */

    //For new users
    public ChannelName (String channelName) {
        this.channelName = channelName;
        hashtagsPublished = new ArrayList<>();
        userVideoFilesMap = new HashMap<>();
    }

    //To create existing channels
    public ChannelName (String channelName, ArrayList<String> hashtagsPublished, HashMap<String, ArrayList<VideoFile>> userVideoFilesMap) {
        this.channelName = channelName;
        this.hashtagsPublished = hashtagsPublished;
        this.userVideoFilesMap = userVideoFilesMap;
    }

    /** Getters */

    /** Setters */
    public void addHashTag(String hashtag) {
        hashtagsPublished.add(hashtag);
    }

    public void removeHashTag(String hashtag) {
        hashtagsPublished.remove(hashtag);
    }
}