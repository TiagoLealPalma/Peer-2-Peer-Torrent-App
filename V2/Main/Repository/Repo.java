package V2.Main.Repository;

import V2.Auxiliary.DownloadRelated.FileBlockResult;
import V2.Auxiliary.Structs.FileMetadata;
import V2.Main.Connection.ConnectionManager;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Repo {
    public File directory;
    private MessageDigest digest;
    public static final int BLOCKSIZE = 10240; // in KB to calculate hash
    private List<FileMetadata> fileMetadataList = new ArrayList<FileMetadata>();
    private static Repo instance;

    public Repo(String repoName) {
        directory = new File("./" + repoName);

        // Corner cases
        if(!directory.exists()){
            directory.mkdir();
            System.out.println("Foi criada um novo pasta para disponibilizar ficheiros: " + directory.getAbsolutePath() +
                    "\n Coloque os ficheiros que pretende disponibilizar e atualize o repositório");
            return;
        }
        if(!directory.isDirectory()){
            System.out.println("O caminho inserido não corresponde a uma directoria: " + directory.getAbsolutePath() +
                    "\n Reinicie a aplicação e insira um caminho válido.");
            return;
        }

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException(e);}

        File[] files = directory.listFiles();
        for(final File file : files){
            try {
                digest.reset();
                byte[] buffer = new byte[BLOCKSIZE];
                int bytesRead = 0;
                int numOfBytes = 0;
                FileInputStream fis = new FileInputStream(file);

                while((bytesRead = fis.read(buffer)) != -1){
                    digest.update(buffer, 0, bytesRead);
                    numOfBytes+=bytesRead;
                }
                fis.close();
                byte[] hash = digest.digest();
                fileMetadataList.add(new FileMetadata(file.getName(), hash, numOfBytes));

            } catch (FileNotFoundException e) {
                System.out.println("File was not found.");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Error occured while building repo.");
                e.printStackTrace();
            }
        }
    }

    public static synchronized Repo getInstance(String filePath) {
        if (instance == null) {
            instance = new Repo(filePath);
        }
        return instance;
    }

    public static synchronized Repo getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Repo has not been initialized yet.");
        }
        return instance;
    }



    public void refreshRepo(){
        fileMetadataList.clear();
        File[] files = directory.listFiles();
        for(final File file : files){
            try {
                digest.reset();
                byte[] buffer = new byte[BLOCKSIZE];
                int bytesRead = 0;
                int numOfBlocks = 0;
                FileInputStream fis = new FileInputStream(file);

                while((bytesRead = fis.read(buffer)) != -1){
                    digest.update(buffer, 0, bytesRead);
                    numOfBlocks++;
                }
                fis.close();
                byte[] hash = digest.digest();
                fileMetadataList.add(new FileMetadata(file.getName(), hash, numOfBlocks));

            } catch (FileNotFoundException e) {
                System.out.println("File was not found.");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Error occured while building repo.");
                e.printStackTrace();
            }
        }
    }

    public ArrayList<FileMetadata> wordSearchResponse(String keyWord) {
        ArrayList<FileMetadata> list = new ArrayList<>();
        if(fileMetadataList.isEmpty()) return list; // In case directory is empty just return empty list

        for (FileMetadata file : fileMetadataList) {
            if(!file.getFileName().equalsIgnoreCase(".ds_store") && file.getFileName().toLowerCase().contains(keyWord.toLowerCase()))
                list.add(file);
        }
        return list;
    }

    public List<FileBlockResult> calculateFileBlocks(FileMetadata file, int preferedBlockSize) {
        int index = fileMetadataList.indexOf(file);
        if(index == -1) return null;

        File fileToDivide = directory.listFiles()[index];
        List<FileBlockResult> results = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(fileToDivide);


        byte[] buffer = new byte[preferedBlockSize];
        int bytesRead = 0;
        int blockIndex = 0;

        while((bytesRead = fis.read(buffer)) != -1){
            byte[] blockData = new byte[bytesRead]; // Create a new array for the block
            System.arraycopy(buffer, 0, blockData, 0, bytesRead); // Copy the data
            results.add(new FileBlockResult(blockData, blockIndex));
            blockIndex++;
            System.out.println("Block " + blockIndex + ": bytesRead = " + bytesRead);

        }

        } catch (FileNotFoundException e) {
            System.out.println("File was not found.");
            return null;
        } catch (IOException e) {
            System.out.println("Error occurred trying to read file");
            return null;
        }
        return results;
    }

    public static void main(String[] args) {
        Repo repo = new Repo("dll2");
        ArrayList<FileMetadata> searchContent = repo.wordSearchResponse("");

        FileMetadata f = searchContent.get(0);
        //repo.writeFile(repo.calculateFileBlocks(searchContent.get(0)),f);

    }
}

