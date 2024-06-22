package librarySystem.patron.patronGUI;

import librarySystem.TabModel;
import librarySystem.patron.Patron;
import librarySystem.patron.PatronHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;

/**
 * Tab for removing manga
 * It has fields for the manga attributes and buttons to remove the manga
 * It implements the TabModel interface
 */
public class RemovePatronTab implements TabModel {
    private final  JFrame frame;
    private final PatronHandler handler;
    private final JTabbedPane tabbedPane;

    private JTextField removeCpfField ;
    private JTextField removeLastNameField ;
    private JPanel removePanel;
    private JButton removeByCpfButton;
    JButton removeByLastNameButton;

    /**
     * Constructor for the RemoveMangaTab
     * It initializes the frame, handler and tabbedPane
     * It creates the tab
     * @param frame the JFrame
     * @param handler the MangaHandler
     * @param tabbedPane the JTabbedPane
     */
    public RemovePatronTab(JFrame frame, PatronHandler handler, JTabbedPane tabbedPane){
        this.frame = frame;
        this.handler = handler;
        this.tabbedPane = tabbedPane;

        createTab();
    }

    /**
     * Method to create the tab
     * It initializes the components and adds the components
     */
    public void createTab() {
        initComponents();
        addComponents();
    }

    /**
     * Method to initialize the components
     */
    public void initComponents() {
        removePanel = new JPanel(new GridLayout(3, 2));
        removePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        removeCpfField = new JTextField();
        removeLastNameField = new JTextField();
        removeByCpfButton = new JButton("Remove by ISBN");
        removeByLastNameButton = new JButton("Remove by LastName");

    }

    /**
     * Method to add the components
     */
    public void addComponents() {
        removePanel.add(new JLabel("CPF:"));
        removePanel.add(removeCpfField);
        removePanel.add(new JLabel("LastName:"));
        removePanel.add(removeLastNameField);

        removeByCpfButton.addActionListener(this);
        removeByLastNameButton.addActionListener(this);
        removePanel.add(removeByCpfButton);
        removePanel.add(removeByLastNameButton);
        tabbedPane.addTab("Remove Patron", removePanel);
    }

    /**
     * Method to handle the actions of the buttons
     * It removes the patron from the list of patrons
     * @param e the ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to remove this patron?");
        if (confirm == JOptionPane.YES_OPTION) {
            if (e.getSource() == removeByCpfButton) {
                try {
                    handler.deletePatron(removeCpfField.getText());
                    JOptionPane.showMessageDialog(frame, "Patron removed successfully!");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error removing patron.");
                }

            }else if (e.getSource() == removeByLastNameButton) {
                try {
                    List<Patron> patrons = handler.searchPatronsByLastName(removeLastNameField.getText());
                    if (!patrons.isEmpty()) {
                        handler.deletePatron(patrons.get(0).getCpf());
                        JOptionPane.showMessageDialog(frame, "Patron removed successfully!");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Patron not found.");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error removing patron.");
                }
            }
        }
    }
}

