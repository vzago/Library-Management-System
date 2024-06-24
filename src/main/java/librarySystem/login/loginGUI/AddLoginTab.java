package librarySystem.login.loginGUI;

import librarySystem.login.Login;
import librarySystem.login.LoginHandler;
import librarySystem.TabModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class AddLoginTab implements TabModel {
    private final JFrame frame;
    private final LoginHandler loginHandler;
    private final JTabbedPane tabbedPane;

    private JPanel addPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton addButton;

    public AddLoginTab(JFrame frame,LoginHandler loginHandler, JTabbedPane tabbedPane) {
        this.frame = frame;
        this.loginHandler = loginHandler;
        this.tabbedPane = tabbedPane;
        createTab();
    }

    @Override
    public void createTab() {
        initComponents();
        addComponents();
    }

    @Override
    public void initComponents() {
        addPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        addButton = new JButton("Add Login");
        addButton.addActionListener(this);
    }

    @Override
    public void addComponents() {
        addPanel.add(new JLabel("Username:"));
        addPanel.add(usernameField);
        addPanel.add(new JLabel("Password:"));
        addPanel.add(passwordField);
        addPanel.add(new JLabel()); // empty label for spacing
        addPanel.add(addButton);
        tabbedPane.addTab("Add Login", addPanel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        try {
            loginHandler.addLogin(new Login(username, password));
            JOptionPane.showMessageDialog(frame, "Login added successfully!");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
        }
    }

}