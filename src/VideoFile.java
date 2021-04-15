import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class VideoFile {

    private String filepath;
    public VideoFile(String filepath){
        this.filepath = filepath;
    }

    public byte[] getVideoFileChunk() {

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
        return this.filepath;
    }
}
