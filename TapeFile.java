public class TapeFile {
    private String filePath;
    private long fileOffset;
    private boolean endOfFile;

    public boolean isEndOfFile() {
        return endOfFile;
    }

    public void setEndOfFile(boolean endOfFile) {
        this.endOfFile = endOfFile;
    }

    public TapeFile(String path){
        this.filePath = path;
        this.fileOffset = 0;
        this.endOfFile = false;
    }

    public String getPath(){
        return this.filePath;
    }

    public long getOffset(){
        return this.fileOffset;
    }

    public void setOffset(long offset){
        this.fileOffset = offset;
    }
}
