package login;

import javax.swing.*;
import java.awt.*;

public class LogInView {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JButton loginButton;
    private JButton signInButton;
    private LogInController controller;

    public LogInView(LogInController controller) {
        this.controller = controller;
        frame = new JFrame("Login");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Main panel with sky blue background and centered layout
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(173, 216, 230)); // Sky blue color
        mainPanel.setLayout(new GridBagLayout());
        frame.add(mainPanel, BorderLayout.CENTER);

        // Add JOBSTER logo centered
        ImageIcon jobsterIcon = new ImageIcon("img/jobster_alt.png"); // Path to your image
        Image scaledImage = jobsterIcon.getImage().getScaledInstance(200, 80, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(scaledImage);
        JLabel logoLabel = new JLabel(resizedIcon);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 20, 10);
        mainPanel.add(logoLabel, gbc);

        // Panel for input fields and buttons
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false); // Make panel transparent to keep sky blue background
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Username:"), gbc);

        usernameField = new JTextField(15);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(loginButton, gbc);
        loginButton.addActionListener(e -> controller.authenticateUser(
                usernameField.getText().trim(), new String(passwordField.getPassword()).trim()));

        signInButton = new JButton("Sign Up");
        gbc.gridx = 1;
        panel.add(signInButton, gbc);
        signInButton.addActionListener(e -> controller.openSignUpView());

        // Add panel to mainPanel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        mainPanel.add(panel, gbc);

        // Status label for login feedback
        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 2;
        mainPanel.add(statusLabel, gbc);

        // Set frame position to center
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void close() {
        frame.dispose();
    }

    public JTextField getUsernameField() {
        return usernameField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public JFrame getFrame() {
        return frame;
    }
}

