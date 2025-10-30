package signup;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class SignUpView {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleDropdown;
    private JButton registerButton;
    private JButton cancelButton;
    private SignUpController controller;

    public SignUpView(SignUpController controller) {
        this.controller = controller;
        frame = new JFrame("Sign Up");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout()); // Use BorderLayout

        // Set background color to light blue
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(173, 216, 230)); // Light blue color
        mainPanel.setLayout(new GridBagLayout()); // Apply GridBagLayout
        frame.add(mainPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // Add Username label and field
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(20);
        mainPanel.add(usernameField, gbc);

        // Add Password label and field
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        mainPanel.add(passwordField, gbc);

        // Add Role label and dropdown
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Role:"), gbc);

        gbc.gridx = 1;
        roleDropdown = new JComboBox<>(new String[]{"Job Finder", "Employer"});
        mainPanel.add(roleDropdown, gbc);

        // Add Register and Cancel buttons in a panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false); // Fix button background issue

        registerButton = new JButton("Register");
        registerButton.addActionListener(e -> {
            try {
                controller.registerUser(
                        usernameField.getText().trim(),
                        new String(passwordField.getPassword()).trim(),
                        (String) roleDropdown.getSelectedItem()
                );
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> controller.openLoginView());

        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private GridBagConstraints setGridBagConstraints(GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        return gbc;
    }

    public void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void displayMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void close() {
        frame.dispose();
    }

    public JFrame getFrame() {
        return frame;
    }
}

