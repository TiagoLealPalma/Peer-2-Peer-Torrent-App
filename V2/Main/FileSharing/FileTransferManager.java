package V2.Main.FileSharing;

import V2.Auxiliary.DownloadRelated.FileBlockResult;
import V2.Auxiliary.DownloadRelated.FileDownloadResponse;
import V2.Auxiliary.Structs.FileMetadata;
import V2.Main.Connection.OpenConnection;
import V2.Main.Coordinator;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileTransferManager {
    private final Map<String, DownloadProcess> openDownloadProcesses;
    public final int THREADS_FOR_UPLOAD;
    private final ExecutorService executor;
    private static FileTransferManager instance;

    private FileTransferManager(int ThreadsAllowedPerDownload) {

        this.openDownloadProcesses = new HashMap<>();
        this.THREADS_FOR_UPLOAD = ThreadsAllowedPerDownload; // Limits the amount of simultaneous upload processes
        executor = Executors.newFixedThreadPool(THREADS_FOR_UPLOAD);

    }

    public static synchronized FileTransferManager getInstance(int ThreadsAllowedPerDownload) {
        if (instance == null) {
            instance = new FileTransferManager(ThreadsAllowedPerDownload);
        }
        return instance;
    }

    public static synchronized FileTransferManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("FileTransferManager has not been initialized yet.");
        }
        return instance;
    }




    // Start a new Download Process
    public void startDownloadProcess(String processId, FileMetadata fileToDownload) {
        openDownloadProcesses.put(processId, new DownloadProcess(this,
                                                                            fileToDownload, processId));
    }

    // Start a new upload task and manage it with the thread pool
    public UploadProcess startUploadProcess(List<FileBlockResult> blocks,
                                                OpenConnection connectionWithDownloadingPeer, String processID) {
        UploadProcess task = new UploadProcess(connectionWithDownloadingPeer, processID, this, blocks);
        executor.submit(task);
        return task;
    }


    // Check if a download process corresponding to this processId is already created, if not created.
    // If it is created, just add a new worker to handle the process
    public void addNewSeederToDownloadProcess(FileDownloadResponse fileDownloadResponse, OpenConnection connection) {
        String processId = fileDownloadResponse.getPROCESS_ID();

        if(openDownloadProcesses.isEmpty() || openDownloadProcesses.get(processId) == null) {
            System.out.println("Tentativa de acesso a um processo de Download inexistente");
            return;
        }
        openDownloadProcesses.get(processId).addWorker(connection);

    }

    public void delieverFileData(PriorityQueue<FileBlockResult> blocks, FileMetadata fileMetadata) {
        Coordinator.getInstance().delieverFileData(blocks, fileMetadata);
    }
}
