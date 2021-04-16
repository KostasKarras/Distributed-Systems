import java.util.ArrayList;

public class Value {

    private VideoFile videoFile;

    public Value(VideoFile videoFile){
        this.videoFile = videoFile;
    }

    public String getFilepath(){
        return this.videoFile.getFilepath();
    }

    public ArrayList<String> getAssociatedHashtags(){
        return this.videoFile.getAssociatedHashtags();
    }

    public void addAssociatedHashTags(String hashtag){
        this.videoFile.addAssociatedHashTags(hashtag);
    }
}
