import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserInterface extends JFrame {
    private JTextField searchField;
    private JList<String> resultList;
    private JButton searchButton, downloadButton, connectButton;
    private String[] searchResults;
    private Controller controller;

    public UserInterface() {
        // Set up the frame
        setTitle("BitTorrent");
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
        searchResults = new String[]{"lazy-day-stylish-futuristic-chill-239827_um.mp3 <2>",
                "soulful-piano-serenade-30s-244335_um.mp3 <1>"};
        resultList = new JList<>(searchResults);
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

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openConnectPortWindow();
            }
        });

        controller = new Controller(this);

        setVisible(true);
    }


    private void openConnectPortWindow(){
        JDialog dialog = new JDialog(this, "Adicionar Nó", true);
        dialog.setSize(550, 60);
        dialog.setLayout(new GridLayout(1,6));
        dialog.add(new JLabel("Endereço:", JLabel.CENTER));
        JTextField address = new JTextField();
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
                controller.requestNewConnection(address.getText(), port.getText());
                dialog.dispose();
                //dialog.setVisible(false);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UserInterface ui = new UserInterface();
            ui.setVisible(true);
        });
    }
}