package main.Repository;

import auxiliary.DownloadRelated.FileBlockResult;
import auxiliary.Structs.FileMetadata;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Repo {
    public File directory;
    private MessageDigest digest;
    public static final int BLOCKSIZE = 10240; // in KB to calculate hash
    private List<FileMetadata> fileMetadataList = new ArrayList<FileMetadata>();
    private static Repo instance;

    public Repo(String repoName) {
        directory = new File("./" + repoName);

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException(e);}

        // Corner cases
        if(!directory.exists()){
            directory.mkdir();
            System.out.println("Foi criada um novo pasta para disponibilizar ficheiros: " + directory.getAbsolutePath() +
                    "\nColoque os ficheiros que pretende disponibilizar e atualize o repositório");
            return;
        }
        if(!directory.isDirectory()){
            System.out.println("O caminho inserido não corresponde a uma directoria: " + directory.getAbsolutePath() +
                    "\n Reinicie a aplicação e insira um caminho válido.");
            return;
        }

        // Build List and FileMetaData for each file present in repository
        refreshRepo();
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
                int numOfBytes = 0;
                FileInputStream fis = new FileInputStream(file);

                while((bytesRead = fis.read(buffer)) != -1){
                    digest.update(buffer, 0, bytesRead);
                    numOfBytes+=bytesRead;
                }
                fis.close();
                byte[] hash = digest.digest();

                // Check if its already present on the list
                boolean alreadyExists = false;
                for (FileMetadata fileMetadata : fileMetadataList) {
                    if(java.util.Arrays.equals(fileMetadata.getHash(), hash)){
                        alreadyExists = true;
                        break;
                    }
                }

                if(!alreadyExists)
                    fileMetadataList.add(new FileMetadata(file.getName(), hash, numOfBytes));

            } catch (FileNotFoundException e) {
                System.err.println("File was not found.");
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("Error occured while building repo.");
                e.printStackTrace();
            }
        }
    }

    public ArrayList<FileMetadata> wordSearchResponse(String keyWord) {
        ArrayList<FileMetadata> list = new ArrayList<>();
        if(fileMetadataList.isEmpty()) return list; // In case directory is empty just return empty list

        for (FileMetadata file : fileMetadataList) {
            if(!file.getFileName().equalsIgnoreCase(".ds_store")
                    && !file.getFileName().equalsIgnoreCase(".gitignore")
                    && file.getFileName().toLowerCase().contains(keyWord.toLowerCase())
            )
                list.add(file);
        }
        return list;
    }

    public List<FileBlockResult> calculateFileBlocks(FileMetadata file, int preferredBlockSize) {
        int index = fileMetadataList.indexOf(file);
        if(index == -1) return null;

        File fileToDivide = directory.listFiles()[index];
        List<FileBlockResult> results = new ArrayList<>();

        // Calculate list of blocks
        try {
            FileInputStream fis = new FileInputStream(fileToDivide);

            int totalBytesRead = 0; // Sets the offset value for a block
            byte[] buffer = new byte[preferredBlockSize]; // Preferred size of the block in a download
            int bytesRead = 0; // Bytes read per iteration, added to offset in the end of iteration

            while((bytesRead = fis.read(buffer)) != -1){
                byte[] blockData = new byte[bytesRead]; // Create a new array for the block
                System.arraycopy(buffer, 0, blockData, 0, bytesRead); // Copy the data
                results.add(new FileBlockResult(blockData, totalBytesRead));
                System.out.println("Block " + totalBytesRead + ": bytesRead = " + bytesRead);
                totalBytesRead+=bytesRead;

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

}
