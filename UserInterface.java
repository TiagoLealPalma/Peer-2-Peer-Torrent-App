import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class UserInterface extends JFrame {
    private JTextField searchField;
    private JList<String> resultList;
    private JButton searchButton, downloadButton, connectButton;
    private String[] searchResults;
    private ArrayList<String> searchResult;
    private ArrayList<String> filteredSearchResult;
    private Controller controller;

    public UserInterface(Controller controller) {
        // Set up the frame
        setTitle("BitTorrent - PORT: " + controller.PORT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLayout(new BorderLayout());


        // Top Panel Components
        JPanel topPanel = new JPanel(new GridLayout(1,3));  // Use BorderLayout to make components stretch

        searchField = new JTextField();
        searchButton = new JButton("Procurar");  // "Search" button

        topPanel.add(new JLabel("Texto a procurar:")); // Label on the left
        topPanel.add(searchField); // Search field will stretch in the center
        topPanel.add(searchButton); // Search button on the right

        // List for search results APAGAR AS SAMPLES!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        searchResult = new ArrayList<>();
        filteredSearchResult = new ArrayList<>();
        resultList = new JList<>(searchResult.toArray(new String[0]));
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
                filterSearchList(searchField.getText());
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
        dialog.setLayout(new GridLayout(1,6));
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
                if(!controller.requestNewConnection(address.getText(), port.getText()))
                    JOptionPane.showMessageDialog(null,"Não foi possivel estabelecer esta ligação");
                else{
                    JOptionPane.showMessageDialog(null,"Ligação estabelecida com sucesso.\n(" +address.getText()+":"+port.getText()+")");
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
    // filter is applied client side, no need to request info based on a filter
    public void filterSearchList(String wordBeingSearched){
        filteredSearchResult.clear();
        for (String title : searchResult){
            if(title.toLowerCase().contains(wordBeingSearched.toLowerCase()))
                filteredSearchResult.add(title);
        }

        SwingUtilities.invokeLater(() -> {
            resultList.setListData(filteredSearchResult.toArray(new String[0]));
        });

    }


    public synchronized void cleanSearchList(){
        searchResult.clear();
    }

    public synchronized void addContentToSearchList(String[] contentToAdd){
        if(Objects.equals(contentToAdd[0], ""))return;
        for (String str : contentToAdd){
            boolean added = false;
            if(searchResult.isEmpty())
                searchResult.add(str + " (1)");
            else {
                Iterator<String> iterator = searchResult.iterator();
                while(iterator.hasNext() && !added){
                    String title = iterator.next();
                    // Incrementar numero de repositorios com esta informação disponivel
                    if (str.equals(title.substring(0, title.length()-4))) {
                        String temp = title.substring(0, str.length())
                                + " ("
                                + (Integer.parseInt(title.substring(title.length() - 2, title.length() - 1)) + 1)
                                + ")";
                        searchResult.remove(title);
                        searchResult.add(temp);
                        added = true;
                        break;
                    }
                }
                if (!added)searchResult.add(str + " (1)");
            }
        }


        filterSearchList(searchField.getText());

    }

}