package V2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Repo {
    private File repo;
    private PrintWriter writer;
    private String[] content;
    private static final int CHUNKSIZE = 256; // in KB
    public class FileMetadata{
        private String fileName;
        private long fileSize;
        private int totalChunks;

        FileMetadata(String fileName, long fileSize, int totalChunks){
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.totalChunks = totalChunks;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            FileMetadata that = (FileMetadata) obj;
            return fileSize == this.fileSize &&
                    totalChunks == this.totalChunks &&
                    fileName.equals(this.fileName);
        }

        @Override
        public String toString() {
            return "FileMetadata{" +
                    "fileName='" + fileName + '\'' +
                    ", fileSize=" + fileSize +
                    ", totalChunks=" + totalChunks +
                    '}';
        }

        public String getFileName() {
            return fileName;
        }

        public long getFileSize() {
            return fileSize;
        }

        public int getTotalChunks() {
            return totalChunks;
        }
    }


    public Repo(String repoName) {
        repo = new File("./" + repoName);
        try {
            writer = new PrintWriter(repo);
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + repoName);
        }
    }

    public ArrayList<String> wordSearchResponse(String keyWord) {
        ArrayList<String> list = new ArrayList<>();
        for (String title : repo.list()) {
            if(title.toLowerCase().contains(keyWord.toLowerCase()))
                list.add(title);
        }
        return list;
    }

    public FileMetadata getFileMetadata(String filename){
        File file = new File(repo, filename);
        if(!file.exists() || !file.isFile() || !file.canRead()){
            System.out.println("File not found or not valid: " + filename);
            return null;
        }

        // Calculate total chunks
        int totalChunks = (int)Math.ceil(file.length() / CHUNKSIZE);


        return new FileMetadata(file.getName(), file.length(), totalChunks);
    }




    public static void main(String[] args) {
        V1.Repo repo = new V1.Repo("dll1");
        ArrayList<String> searchContent = repo.getSearchContent();

        System.out.println(repo.getFileMetadata(searchContent.get(0)));
    }
}

