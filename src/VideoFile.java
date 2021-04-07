import java.util.ArrayList;

public class VideoFile {

    private String videoName;
    private String channelName;
    private String dateCreated;
    private String length;
    private String frameRate;
    private String frameWidth;
    private String frameHeight;
    private ArrayList<String> associatedHashtags;
    private Byte[] videoFileChunk;

    public VideoFile(String videoName, String channelName, String dateCreated, String length, String frameRate, String frameWidth,
                     String frameHeight, ArrayList<String> associatedHashtags, Byte[] videoFileChunk) {
        this.videoName = videoName;
        this.channelName = channelName;
        this.dateCreated = dateCreated;
        this.length = length;
        this.frameRate = frameRate;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.associatedHashtags = associatedHashtags;
        this.videoFileChunk = videoFileChunk;
    }
}