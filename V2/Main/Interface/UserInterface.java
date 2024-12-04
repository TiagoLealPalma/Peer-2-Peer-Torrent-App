package V2.Main.Interface;

import V2.Auxiliary.Structs.FileMetadata;
import V2.Main.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;

public class UserInterface extends JFrame {
    private JTextField searchField;
    private JList<String> resultList;
    private JButton searchButton, downloadButton, connectButton;
    private Map<FileMetadata, Integer> titles;
    private ArrayList<String> toDisplay;
    private Controller controller;

    public UserInterface(Controller controller) {
        // Set up the frame
        setTitle("BitTorrent - PORT: " + controller.PORT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLayout(new BorderLayout());


        // Top Panel Components
        JPanel topPanel = new JPanel(new FlowLayout());

        searchField = new JTextField(35);
        searchButton = new JButton("Procurar");
        JButton refreshRepo = new JButton("Refresh Repo");

        topPanel.add(refreshRepo);
        topPanel.add(new JLabel("Texto a procurar:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);

        // List for search results
        titles = new HashMap<>();
        toDisplay = new ArrayList<>();
        resultList = new JList<>(toDisplay.toArray(new String[0]));
        JScrollPane scrollPane = new JScrollPane(resultList);

        // Right Panel Components
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1));
        downloadButton = new JButton("Descarregar");
        connectButton = new JButton("Ligar a Nó");

        buttonPanel.add(downloadButton);
        buttonPanel.add(connectButton);

        // Add panels to the frame
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);

        setLocationRelativeTo(null);

        // Actions

        // Search button action listener - Gets the key word and passes it to the controller so it can run the
        // search across all the peers
        searchButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                titles.clear();
                controller.filterSearchList(searchField.getText());
            }
        });

        // Connect button action listener - Opens a pop-up window that has inputs for the specification
        // of the connection
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    openConnectPortWindow();
                } catch (UnknownHostException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Refresh Repo action listener - Updates the files present in the repo, so new files can be loaded
        // and shared across the peers
        refreshRepo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.refreshRepo();
            }
        });

        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Gets the selected value from the JList
                String selectedValue = resultList.getSelectedValue();

                if (selectedValue == null) {
                    JOptionPane.showMessageDialog(null, "Por favor, selecione um item para descarregar.");
                    return;
                }

                // Extracts the file name from the selected value (removing the counterpart if necessary)
                String fileName = selectedValue.split(" <")[0];

                // Finds the associated FileMetadata object
                FileMetadata fileToDownload = null;
                for (FileMetadata file : titles.keySet()) {
                    if (file.getFileName().equals(fileName)) {
                        fileToDownload = file;
                        break;
                    }
                }

                // Check if we found the FileMetadata object
                if (fileToDownload != null) {
                    // Pass the FileMetadata object to the controller to start the download
                    controller.initiateDownload(fileToDownload);
                } else {
                    JOptionPane.showMessageDialog(null, "Erro: o item selecionado não foi encontrado.");
                }
            }
        });


        this.controller = controller;

        setVisible(true);
    }


    private void openConnectPortWindow() throws UnknownHostException {
        JDialog dialog = new JDialog(this, "Adicionar Nó", true);
        dialog.setSize(550, 60);
        dialog.setLayout(new GridLayout(1, 6));
        dialog.add(new JLabel("Endereço:", JLabel.CENTER));
        JTextField address = new JTextField(InetAddress.getByName(null).getHostAddress());
        dialog.add(address);
        dialog.add(new JLabel("Porta:", JLabel.CENTER));
        JTextField port = new JTextField();
        dialog.add(port);
        JButton cancelButton = new JButton("Cancelar");
        JButton okButton = new JButton("Ok");
        dialog.add(cancelButton);
        dialog.add(okButton);
        dialog.setLocationRelativeTo(null);


        // Action Listeners
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int parsedPort;
                try{ parsedPort = Integer.parseInt(port.getText());} catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Insira um porto válido.");
                    return;
                }

                int value = controller.requestNewConnection(address.getText(), parsedPort, searchField.getText());

                // Non-Valid Connection
                if(value < 20) {
                    if(value == 11)
                        JOptionPane.showMessageDialog(null, "Conexão já se encontra estabelecida.");
                    else if(value == 12)
                        JOptionPane.showMessageDialog(null, "Se procura uma ligação consigo próprio, tente o psicolgo :)");
                    else if (value == 13)
                        JOptionPane.showMessageDialog(null, "Use portos entre 8080 e 65535.");
                }
                // Successful Connection
                else if (value == 20) {
                    JOptionPane.showMessageDialog(null, "Ligação estabelecida com sucesso.\n(" + address.getText() + ":" + port.getText() + ")");
                    dialog.dispose();
                }
                // Connection Failed
                else if (value >= 30)
                    JOptionPane.showMessageDialog(null, "Não foi possivel estabelecer esta ligação.");

            }
        });

        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        dialog.setVisible(true);
    }

    // Filter is applied client side, no need to request info based on a filter
    public synchronized void addContentToSearchList(List<FileMetadata> list) {
        // Merges the new list with the existing map
        for (FileMetadata file : list) {
           titles.merge(file, 1, Integer:: sum);
        }

        // Prepare to display content
        toDisplay.clear();
        for (Map.Entry<FileMetadata, Integer> entry : titles.entrySet()) {
            toDisplay.add(entry.getKey().getFileName() + " <" + entry.getValue() + ">");
        }

        SwingUtilities.invokeLater(() -> {
            resultList.setListData(toDisplay.toArray(new String[0]));
        });
    }

    public void showDownloadInfo(Map<Integer,Integer> blocksPerSeeder) {
        String results = "Download efetuado com sucesso!";
        for (Integer port : blocksPerSeeder.keySet()) {
            results += String.format("\n Peer %d entregou %d blocos para este download.", port, blocksPerSeeder.get(port));
        }


        JOptionPane.showMessageDialog(null,results);
    }

    public void popUpPrint(String string){
        JOptionPane.showMessageDialog(null, string);
    }
}
