package V2.Main.Repository;

import V2.Auxiliary.MessageTypes.DownloadRelated.FileBlockResult;
import V2.Auxiliary.Structs.FileMetadata;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Repo {
    private File directory;
    private MessageDigest digest;
    private static final int BLOCKSIZE = 10240; // in KB
    private List<FileMetadata> fileMetadataList = new ArrayList<FileMetadata>();

    public Repo(String repoName) {
        directory = new File("./" + repoName);

        // Corner cases
        if(!directory.exists()){
            directory.mkdir();
            System.out.println("Foi criada um novo pasta para disponibilizar ficheiros: " + directory.getAbsolutePath());
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

    public List<FileBlockResult> calculateFileBlocks(FileMetadata file) {
        int index = fileMetadataList.indexOf(file);
        if(index == -1) return null;

        File fileToDivide = directory.listFiles()[index];
        List<FileBlockResult> results = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(fileToDivide);


        byte[] buffer = new byte[BLOCKSIZE];
        int bytesRead = 0;
        int blockIndex = 0;

        while((bytesRead = fis.read(buffer)) != -1){
            byte[] blockData = new byte[bytesRead]; // Create a new array for the block
            System.arraycopy(buffer, 0, blockData, 0, bytesRead); // Copy the data
            results.add(new FileBlockResult(blockData, blockIndex));
            blockIndex++;
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

    // Takes te priority queue and writes the file in the directory
    public boolean writeFile(PriorityQueue<FileBlockResult> data, FileMetadata fileMetadata) {
        try {
            // Make sure the file is written even if it's a duplicate by adding, for exemple, (1) after
            String originalPath = directory.getAbsolutePath() + "/" + fileMetadata.getFileName();
            File file = new File(originalPath);
            String path = originalPath;
            int index = 1;

            // Check if the file exists, and append "(1)", "(2)", etc., if it does
            while (file.exists()) {
                int dotIndex = originalPath.lastIndexOf('.');
                if (dotIndex == -1) {
                    // Just appends the number
                    path = originalPath + " (" + index + ")";
                } else {
                    // Add the number before the extension
                    path = originalPath.substring(0, dotIndex) + " (" + index + ")" + originalPath.substring(dotIndex);
                }
                file = new File(path);
                index++;
            }
            FileOutputStream fos = new FileOutputStream(path);
            for(FileBlockResult block : data) {
                fos.write(block.getBlock());
            }
            System.out.println("File ("+fileMetadata.getFileName()+") written successfully.");
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
            return false;
        }
        return true;
    }


    public static void main(String[] args) {
        Repo repo = new Repo("dll2");
        ArrayList<FileMetadata> searchContent = repo.wordSearchResponse("");

        FileMetadata f = searchContent.get(0);
        //repo.writeFile(repo.calculateFileBlocks(searchContent.get(0)),f);

    }
}

