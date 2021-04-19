import java.io.*;
import java.util.ArrayList;

public class VideoFile {

    /** Class Variables */
    private String filepath;
    private ArrayList<String> associatedHashtags;
    private int videoID;

    /** Constructor */
    public VideoFile (String filepath, ArrayList<String> associatedHashtags) {
        this.filepath = filepath;
        this.associatedHashtags = associatedHashtags;
    }

    public byte[] getVideoFileChunk() {
        String filepath = this.getFilepath();
        byte[] buffer = null;
        try {
            File file = new File(filepath);
            FileInputStream fis = new FileInputStream(file);
            buffer = new byte[(int)file.length()];
            fis.read(buffer);
            fis.close();
            return buffer;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /** Getters */
    public String getFilepath() {
        return this.filepath;
    }

    public ArrayList<String> getAssociatedHashtags(){
        return associatedHashtags;
    }

    public int getVideoID() {
        return videoID;
    }

    /** Setters */
    public void addAssociatedHashTags(String hashtag){
        associatedHashtags.add(hashtag);
    }

    public void setVideoID(int videoID) {
        this.videoID = videoID;
    }
}
