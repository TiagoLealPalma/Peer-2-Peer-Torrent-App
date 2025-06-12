package main.FileSharing;

import auxiliary.DownloadRelated.FileBlockResult;
import auxiliary.Structs.FileMetadata;
import main.Interface.UserInterface;
import main.Repository.Repo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class FileWriter extends Thread{
    private final File directory;
    private final DownloadProcess download;
    private final PriorityQueue<FileBlockResult> data;
    private final int blocksExpected;
    private final FileMetadata fileMetadata;
    private long startTime = System.currentTimeMillis();
    private HashMap<DownloadWorker, Integer> workerData = new HashMap<>();

    public FileWriter(DownloadProcess download, int blocksExpected, FileMetadata fileMetadata) {
        this.directory = Repo.getInstance().directory;
        this.download = download;
        this.blocksExpected = blocksExpected;
        this.fileMetadata = fileMetadata;
        data = new PriorityQueue<>();

        start();
    }

    @Override
    public void run() {
        while(data.size() < blocksExpected) {
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        writeFile();
        displaySuccessMessage();
        FileTransferManager.getInstance().cleanUp(download.PROCESS_ID);
        Repo.getInstance().refreshRepo();

    }

    public synchronized void putBlock(FileBlockResult block, DownloadWorker worker){
        workerData.put(worker, workerData.getOrDefault(worker, 0)+1);
        data.add(block);
        notifyAll();
    }


    // Takes te priority queue and writes the file in the directory
    public boolean writeFile() {
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
            while(!data.isEmpty()) {
                FileBlockResult block = data.poll();
                fos.write(block.getBlock());
            }
            System.out.println("File ("+fileMetadata.getFileName()+") written successfully.");
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
            return false;
        }
        return true;

    }


    private void displaySuccessMessage() {
        String toDisplay = "Download concluido com sucesso!";
        for(Map.Entry<DownloadWorker, Integer> entry : workerData.entrySet()) {
            toDisplay = toDisplay + "\nPorto " + entry.getKey().getConnection().getCorrespondentPort() + ": " + entry.getValue() + " blocos enviados";
        }
        toDisplay += "\nTempo de Download: " + (System.currentTimeMillis() - startTime) + "ms";
        UserInterface.getInstance().popUpMessage(toDisplay);
    }

}


/* System.out.println("("+fileMetadata.getFileName()+") Bloco adicionado " + data.size() + "/" + blocksExpected);
System.out.println(block.getOffset());
System.out.println("Vou começar a escrever");


 */