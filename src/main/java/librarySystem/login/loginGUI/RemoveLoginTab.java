package librarySystem.login.loginGUI;

import librarySystem.login.Login;
import librarySystem.login.LoginHandler;
import librarySystem.TabModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class RemoveLoginTab implements TabModel {
    private final JFrame frame;
    private final LoginHandler loginHandler;
    private final JTabbedPane tabbedPane;

    private JPanel removePanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton removeButton;

    public RemoveLoginTab(JFrame frame,LoginHandler loginHandler, JTabbedPane tabbedPane) {
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
        removePanel = new JPanel(new GridLayout(3, 2, 10, 10));
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        removeButton = new JButton("Remove Login");
        removeButton.addActionListener(this);
    }

    @Override
    public void addComponents() {
        removePanel.add(new JLabel("Username:"));
        removePanel.add(usernameField);
        removePanel.add(new JLabel("Password:"));
        removePanel.add(passwordField);
        removePanel.add(new JLabel()); // empty label for spacing
        removePanel.add(removeButton);
        tabbedPane.addTab("Remove Login", removePanel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        try {
            loginHandler.removeLogin(new Login(username, password));
            JOptionPane.showMessageDialog(frame, "Login removed successfully!");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
        }
    }

}