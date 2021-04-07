import java.util.ArrayList;
import java.util.HashMap;

public class ChannelName {

    private String channelName;
    private ArrayList<String> hashtagsPublished;
    private HashMap<String, ArrayList<Value>> userVideoFilesMap;

    public ChannelName(String channelName, ArrayList<String> hashtagsPublished, HashMap<String, ArrayList<Value>> userVideoFilesMap){
        this.channelName = channelName;
        this.hashtagsPublished = hashtagsPublished;
        this.userVideoFilesMap = userVideoFilesMap;
    }
}