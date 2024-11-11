package V2;

import V2.Structs.FileMetadata;

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
    private V2.Controller controller;

    public UserInterface(V2.Controller controller) {
        // Set up the frame
        setTitle("BitTorrent - PORT: " + controller.PORT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLayout(new BorderLayout());


        // Top Panel Components
        JPanel topPanel = new JPanel(new GridLayout(1, 3));  // Use BorderLayout to make components stretch

        searchField = new JTextField();
        searchButton = new JButton("Procurar");  // "Search" button

        topPanel.add(new JLabel("Texto a procurar:")); // Label on the left
        topPanel.add(searchField); // Search field will stretch in the center
        topPanel.add(searchButton); // Search button on the right

        // List for search results APAGAR AS SAMPLES!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        titles = new HashMap<>();
        toDisplay = new ArrayList<>();
        resultList = new JList<>(toDisplay.toArray(new String[0]));
        JScrollPane scrollPane = new JScrollPane(resultList);

        // Right Panel Components
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1));
        downloadButton = new JButton("Descarregar");  // "Download" button
        connectButton = new JButton("Ligar a Nó");    // "Connect to Node" button

        buttonPanel.add(downloadButton);
        buttonPanel.add(connectButton);

        // Add panels to the frame
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);

        setLocationRelativeTo(null);

        // Actions

        searchButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                titles.clear();
                controller.filterSearchList(searchField.getText());
            }
        });

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
                    parsedPort = -1;
                }
                if(parsedPort == controller.PORT) {
                    JOptionPane.showMessageDialog(null, "Se procura uma ligação consigo próprio, tente o psicolgo :)");
                    return;
                }
                if(parsedPort < 8080 || parsedPort > 65535) {
                    JOptionPane.showMessageDialog(null, "Use portos entre 8080 e 65535");
                    return;
                }

                if (!controller.requestNewConnection(address.getText(), parsedPort, searchField.getText()) || parsedPort == -1)
                    JOptionPane.showMessageDialog(null, "Não foi possivel estabelecer esta ligação");
                else {
                    JOptionPane.showMessageDialog(null, "Ligação estabelecida com sucesso.\n(" + address.getText() + ":" + port.getText() + ")");
                    dialog.dispose();
                }
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
    public void addContentToSearchList(List<FileMetadata> list) {
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
}
