package V2;

import V2.Structs.FileMetadata;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Repo {
    private File directory;
    private MessageDigest digest;
    private static final int CHUNKSIZE = 10240; // in KB
    private List<FileMetadata> FileMetadataList = new ArrayList<FileMetadata>();

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
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                FileInputStream fis = new FileInputStream(file);

                while((bytesRead = fis.read(buffer)) != -1){
                    digest.update(buffer, 0, bytesRead);
                }
                fis.close();
                byte[] hash = digest.digest();
                FileMetadataList.add(new FileMetadata(file.getName(), hash));

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
        FileMetadataList.clear();
        File[] files = directory.listFiles();
        for(final File file : files){
            try {
                digest.reset();
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                FileInputStream fis = new FileInputStream(file);

                while((bytesRead = fis.read(buffer)) != -1){
                    digest.update(buffer, 0, bytesRead);
                }
                fis.close();
                byte[] hash = digest.digest();
                FileMetadataList.add(new FileMetadata(file.getName(), hash));

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
        if(FileMetadataList.isEmpty()) return list; // In case directory is empty just return empty list

        for (FileMetadata file : FileMetadataList) {
            if(!file.getFileName().equalsIgnoreCase(".ds_store") && file.getFileName().toLowerCase().contains(keyWord.toLowerCase()))
                list.add(file);
        }
        return list;
    }


    public static void main(String[] args) {
        V2.Repo repo = new V2.Repo("dll1");
        ArrayList<FileMetadata> searchContent = repo.wordSearchResponse("");

        System.out.println(searchContent.get(0).toString());
        System.out.println(searchContent.get(0).getHash());
    }
}

