package admin.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class AdminDashboardView {
    private JFrame frame;
    private JButton userButton;
    private JButton postButton;
    private JButton turnOffButton;

    public AdminDashboardView() {
        // Create the frame
        frame= new JFrame("Jobster Admin Panel");
        frame.setSize(500, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Set background color
        JPanel panel = new JPanel();
        panel.setBackground(new Color(175, 220, 230)); // Light blue
        panel.setLayout(null);
        frame.setContentPane(panel);

        // Add JOBSTER logo
        ImageIcon jobsterIcon = new ImageIcon("img/jobster_alt.png"); // Change to your image path
        Image scaledImage = jobsterIcon.getImage().getScaledInstance(200, 80, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(scaledImage);
        JLabel logoLabel = new JLabel(resizedIcon);
        logoLabel.setBounds(145, 10, 200, 80); // Correct position and size
        frame.add(logoLabel);

        // Manage Users button
        userButton = new JButton("Manage Users");
        userButton.setBounds(120, 120, 120, 30);
        panel.add(userButton);

        // Manage Posts button
        postButton = new JButton("Manage Posts");
        postButton.setBounds(250, 120, 120, 30);
        panel.add(postButton);

        // Turn Off Server button
        turnOffButton = new JButton("Turn Off Server");
        turnOffButton.setBounds(165, 180, 150, 30);
        panel.add(turnOffButton);
    }
    public void addUserButtonListener(ActionListener listener) {
        userButton.addActionListener(listener);
    }

    public void addPostButtonListener(ActionListener listener) {
        postButton.addActionListener(listener);
    }

    public void addTurnOffButtonListener(ActionListener listener) {
        turnOffButton.addActionListener(listener);
    }

    public void close() {
        frame.dispose();
    }

    public void setVisible(boolean isVisible) {
        frame.setVisible(isVisible);
    }

    public JFrame getFrame() {
        return frame;
    }
}
