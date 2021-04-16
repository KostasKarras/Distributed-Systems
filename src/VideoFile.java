import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class VideoFile {

    private ArrayList<String> associatedHashtags;
    private String filepath;
    //private ArrayList<String> associatedHashtags;
    public VideoFile(String filepath, ArrayList<String> associatedHashtags) {
        this.filepath = filepath;
        this.associatedHashtags = associatedHashtags;
    }

    public byte[] getVideoFileChunk() {
        String filepath = this.getFilepath();
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

    public String getFilepath() {
        return filepath;
    }

    public ArrayList<String> getAssociatedHashtags(){
        return associatedHashtags;
    }

    public void addAssociatedHashTags(String hashtag){
        associatedHashtags.add(hashtag);
    }
}
