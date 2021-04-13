import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    private byte[] videoFileChunk;

    public VideoFile(String videoName, String channelName, String dateCreated, String length, String frameRate, String frameWidth,
                     String frameHeight, ArrayList<String> associatedHashtags, byte[] videoFileChunk) {
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

    public byte[] getVideoFileChunk(String filepath) {
        try {
            FileInputStream fin = new FileInputStream(new File(filepath));
            byte[] buffer = new byte[(int)new File(filepath).length()];
            fin.read(buffer);
            fin.close();
            return buffer;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}
