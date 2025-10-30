package admin.view;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.*;

public class AdminPostView extends Component {
    private JFrame frame;
    private JTextField searchField;
    private JButton searchButton, deleteButton, backButton, refreshButton;
    private JButton addPostButton, editPostButton;
    private JTable postTable;
    private DefaultTableModel tableModel;

    public AdminPostView() {
        frame = new JFrame("Jobster Admin Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(135, 206, 235));
        leftPanel.setLayout(null);
        leftPanel.setPreferredSize(new Dimension(250, frame.getHeight()));

        // Logo and buttons
        ImageIcon originalIcon = new ImageIcon("img/jobster.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(scaledImage);
        JLabel logoLabel = new JLabel(resizedIcon);
        logoLabel.setBounds(50, 20, 150, 150);
        leftPanel.add(logoLabel);

        searchField = new JTextField("Search for Job Posts");
        searchField.setBounds(25, 180, 200, 30);
        leftPanel.add(searchField);

        searchButton = new JButton("Search");
        searchButton.setBounds(72, 220, 100, 30);
        leftPanel.add(searchButton);

        deleteButton = new JButton("Delete Post");
        deleteButton.setBounds(60, 280, 125, 30);
        leftPanel.add(deleteButton);

        addPostButton = new JButton("Add Post");
        addPostButton.setBounds(60, 320, 125, 30);
        leftPanel.add(addPostButton);

        editPostButton = new JButton("Edit Post");
        editPostButton.setBounds(60, 360, 125, 30);
        leftPanel.add(editPostButton);

        backButton = new JButton("Back");
        backButton.setBounds(35, 500, 80, 30);
        leftPanel.add(backButton);

        refreshButton = new JButton("Refresh");
        refreshButton.setBounds(130, 500, 80, 30);
        leftPanel.add(refreshButton);

        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setLayout(null);

        // Table with columns for job post data
        String[] columnNames = {"Title", "Company Name", "Job Location", "Max Applicants",
                "Salary", "Description", "Status", "Employer"};
        tableModel = new DefaultTableModel(columnNames, 0);

        postTable = new JTable(tableModel);
        postTable.setCellSelectionEnabled(true);
        postTable.setRowSelectionAllowed(true);
        postTable.setColumnSelectionAllowed(true);

        JScrollPane scrollPane = new JScrollPane(postTable);
        scrollPane.setBounds(20, 20, 580, 500);
        contentPanel.add(scrollPane);

        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(contentPanel, BorderLayout.CENTER);
        frame.setVisible(true);

        // Add TableModelListener to handle edits
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    String newValue = (String) tableModel.getValueAt(row, column);
                    String Title = (String) tableModel.getValueAt(row, 0); // Title column
                    String companyName = (String) tableModel.getValueAt(row, 1); // Company Name column

                    // Update the corresponding field in the JSON file
                    updateJobFieldInJson(Title, companyName, column, newValue);
                }
            }
        });
    }

    private void updateJobFieldInJson(String Title, String companyName, int column, String newValue) {
        try {
            // Path to the JSON file
            File file = new File("res/JobPosts.json");
            if (!file.exists()) {
                JOptionPane.showMessageDialog(frame, "Job history file not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Read the existing JSON data
            FileReader reader = new FileReader(file);
            JsonElement parsedElement = JsonParser.parseReader(reader);
            reader.close();

            if (parsedElement.isJsonObject()) {
                JsonObject rootObject = parsedElement.getAsJsonObject();
                if (rootObject.has("Jobs") && rootObject.get("Jobs").isJsonArray()) {
                    JsonArray jobsArray = rootObject.getAsJsonArray("Jobs");
                    boolean jobUpdated = false;

                    // Get the list of all valid employers (those with job postings)
                    Set<String> validEmployers = new HashSet<>();
                    for (JsonElement jobElement : jobsArray) {
                        JsonObject job = jobElement.getAsJsonObject();
                        String employer = job.get("Employer").getAsString();
                        validEmployers.add(employer);
                    }

                    // Store the original employer name (in case we need to revert it)
                    String originalEmployer = null;

                    // Find the job that matches the title and company
                    for (JsonElement jobElement : jobsArray) {
                        JsonObject job = jobElement.getAsJsonObject();
                        if (job.get("Title").getAsString().equals(Title) &&
                                job.get("CompanyName").getAsString().equals(companyName)) {

                            // Save the original employer name before making any changes
                            originalEmployer = job.get("Employer").getAsString();

                            // Check if the employer name is valid
                            if (column == 7 && !validEmployers.contains(newValue)) { // Employer column index is 7
                                JOptionPane.showMessageDialog(frame, "Invalid employer name! This employer does not exist in the job postings.", "Error", JOptionPane.ERROR_MESSAGE);
                                // Revert the employer name to the original value if invalid
                                job.addProperty("Employer", originalEmployer);
                                return; // Do not proceed with the update
                            }

                            // Update the field based on the column index
                            switch (column) {
                                case 0:
                                    job.addProperty("Title", newValue);
                                    break;
                                case 1:
                                    job.addProperty("CompanyName", newValue);
                                    break;
                                case 2:
                                    job.addProperty("JobLocation", newValue);
                                    break;
                                case 3:
                                    job.addProperty("MaxApplicants", Integer.parseInt(newValue));
                                    break;
                                case 4:
                                    job.addProperty("Salary", Integer.parseInt(newValue));
                                    break;
                                case 5:
                                    job.addProperty("Description", newValue);
                                    break;
                                case 6:
                                    job.addProperty("Status", newValue);
                                    break;
                                case 7:
                                    job.addProperty("Employer", newValue);
                                    break;
                            }

                            jobUpdated = true;
                            break;
                        }
                    }

                    if (jobUpdated) {
                        // Write the updated data back to the JSON file
                        try (FileWriter writer = new FileWriter("res/JobPosts.json")) {
                            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(rootObject));
                            JOptionPane.showMessageDialog(frame, "Job updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(frame, "Error saving the updated job!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Job not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error updating the job!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateTable(Object[][] data) {
        tableModel.setRowCount(0);
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
    }

    public String getSearchText() {
        return searchField.getText().trim();
    }

    public int getSelectedRowIndex() {
        return postTable.getSelectedRow();
    }

    public String getSelectedTitle() {
        int selectedRow = postTable.getSelectedRow();
        return (selectedRow != -1) ? tableModel.getValueAt(selectedRow, 0).toString() : null;
    }

    public void addRefreshListener(ActionListener listener) {
        refreshButton.addActionListener(listener);
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

    public void addDeleteListener(ActionListener listener) {
        deleteButton.addActionListener(listener);
    }

    public void addAddPostListener(ActionListener listener) {
        addPostButton.addActionListener(listener);
    }

    public void addEditPostListener(ActionListener listener) {
        editPostButton.addActionListener(listener);
    }

    public void removeSelectedRow(int rowIndex) {
        if (rowIndex != -1) {
            tableModel.removeRow(rowIndex);
        }
    }

    public void setVisible(boolean isVisible) {
        frame.setVisible(isVisible);
    }

    public void close() {
        frame.dispose();
    }

    public JTable getPostTable() {
        return postTable;
    }

    public JFrame getFrame() {
        return frame;
    }
}