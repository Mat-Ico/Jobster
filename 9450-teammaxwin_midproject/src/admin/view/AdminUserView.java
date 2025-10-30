package admin.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

public class AdminUserView {
    private JFrame frame;
    private JButton addUserButton, deleteUserButton, editUserButton, backButton, refreshButton, searchButton;
    private JTextField searchField;
    private JLabel logoLabel;
    private JTable userTable;
    private DefaultTableModel tableModel;

    public AdminUserView() {
        // Create the frame
        frame = new JFrame("Jobster Admin Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        // Left panel with sky blue background
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(135, 206, 235)); // Sky blue color
        leftPanel.setLayout(null);
        leftPanel.setPreferredSize(new Dimension(250, frame.getHeight()));

        // Load and resize the Jobster logo
        ImageIcon originalIcon = new ImageIcon("img/jobster.png"); // Path to logo image
        Image scaledImage = originalIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(scaledImage);

        // Add Jobster logo
        logoLabel = new JLabel(resizedIcon);
        logoLabel.setBounds(50, 20, 150, 150);
        leftPanel.add(logoLabel);

        // Search field
        searchField = new JTextField("Search for User");
        searchField.setBounds(25, 180, 200, 30);
        leftPanel.add(searchField);

        // Search button below the search field
        searchButton = new JButton("Search");
        searchButton.setBounds(72, 220, 100, 30);
        leftPanel.add(searchButton);

        // Buttons
        addUserButton = new JButton("Add User");
        addUserButton.setBounds(60, 280, 125, 30);
        leftPanel.add(addUserButton);

        deleteUserButton = new JButton("Delete User");
        deleteUserButton.setBounds(60, 320, 125, 30);
        leftPanel.add(deleteUserButton);

        editUserButton = new JButton("Edit User");
        editUserButton.setBounds(60, 360, 125, 30);
        leftPanel.add(editUserButton);

        backButton = new JButton("Back");
        backButton.setBounds(35, 500, 80, 30);
        leftPanel.add(backButton);

        refreshButton = new JButton("Refresh");
        refreshButton.setBounds(130, 500, 80, 30);
        leftPanel.add(refreshButton);

        // Main content panel (right side)
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setLayout(null);

        // Table setup
        String[] columnNames = {"Username", "Password", "Role"};
        tableModel = new DefaultTableModel(columnNames, 0);
        userTable = new JTable(tableModel);

        // Scroll pane for the table
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBounds(20, 20, 580, 500);
        contentPanel.add(scrollPane);

        // Add panels to the frame
        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(contentPanel, BorderLayout.CENTER);

        // Make the frame visible
        frame.setVisible(true);
    }

    // Get search field text
    public String getSearchText() {
        return searchField.getText().trim();
    }

    // Get the table object
    public JTable getUserTable() {
        return userTable;
    }

    // Get username, password, and role at the selected row
    public String getUsernameAtRow(int row) {
        return (String) tableModel.getValueAt(row, 0);
    }

    public String getPasswordAtRow(int row) {
        return (String) tableModel.getValueAt(row, 1);
    }

    public String getRoleAtRow(int row) {
        return (String) tableModel.getValueAt(row, 2);
    }

    // Get selected user from the table
    public String getSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow != -1) {
            return (String) tableModel.getValueAt(selectedRow, 0);
        }
        return null;
    }

    // Action Listeners for buttons
    public void addAddUserListener(ActionListener listener) {
        addUserButton.addActionListener(listener);
    }

    public void addDeleteUserListener(ActionListener listener) {
        deleteUserButton.addActionListener(listener);
    }

    public void addEditUserListener(ActionListener listener) {
        editUserButton.addActionListener(listener);
    }

    public void addSearchListener(ActionListener listener) {
        searchField.addActionListener(listener);
    }

    public void addSearchButtonListener(ActionListener listener) {
        searchButton.addActionListener(listener);
    }

    public void addBackListener(ActionListener listener) {
        backButton.addActionListener(listener);
    }

    public void addRefreshListener(ActionListener listener) {
        refreshButton.addActionListener(listener);
    }

    public void close() {
        frame.dispose();
    }

    // Removed unnecessary 'view' field and used 'this' to refer to the current instance
    public String getUserTypeAtRow(int selectedRow) {
        // Assuming the second column (index 1) contains the user type
        Object userTypeObj = this.userTable.getValueAt(selectedRow, 1);  // Get value at selected row, second column
        if (userTypeObj != null) {
            return userTypeObj.toString();  // Return the user type as a string
        }
        return "";  // Return an empty string if no user type is found
    }
}
