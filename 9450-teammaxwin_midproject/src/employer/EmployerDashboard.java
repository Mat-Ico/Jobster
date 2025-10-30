package employer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.io.*;
import com.google.gson.*;

public class EmployerDashboard extends JFrame {
    private JButton addJobButton, updateJobButton, deleteJobButton, viewActiveJobsButton, viewClosedJobsButton, logoutButton, searchButton, viewApplicantsButton;
    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JTable jobTable;

    public EmployerDashboard() {
        setTitle("Employer Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Left Panel
        JPanel leftPanel = createLeftPanel();
        add(leftPanel, BorderLayout.WEST);

        // Job Display Area
        JPanel jobDisplayArea = createJobDisplayArea();
        add(jobDisplayArea, BorderLayout.CENTER);

        // Add TableModelListener to detect cell edits
        tableModel = (DefaultTableModel) jobTable.getModel();
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    String newValue = (String) tableModel.getValueAt(row, column);
                    String jobTitle = (String) tableModel.getValueAt(row, 1);
                    String companyName = (String) tableModel.getValueAt(row, 2);

                    // Update the corresponding field in the JSON file
                    updateJobFieldInJson(jobTitle, companyName, column, newValue);
                }
            }
        });

        setVisible(true);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(135, 206, 235)); // Sky blue color
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(250, getHeight()));

        // Logo
        ImageIcon originalIcon = new ImageIcon("img/jobster.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
        logoLabel.setBounds(24, 32, 200, 200);
        panel.add(logoLabel);

        // Search Field
        searchField = new JTextField("Search your hiring application!");
        searchField.setBounds(40, 242, 166, 26);
        panel.add(searchField);

        searchButton = new JButton("Search");
        searchButton.setBounds(75, 273, 100, 26);
        panel.add(searchButton);

        // Buttons
        addJobButton = createButton("Add Job Post", 48, 315, panel);
        updateJobButton = createButton("Update Job", 48, 350, panel);
        deleteJobButton = createButton("Delete Job", 48, 387, panel);
        viewActiveJobsButton = createButton("View Active Jobs", 48, 421, panel);
        viewClosedJobsButton = createButton("View Closed Jobs", 48, 454, panel);
        viewApplicantsButton = createButton("View Applicants", 48, 487, panel); // New Button
        logoutButton = createButton("Logout", 48, 520, panel);

        return panel;
    }

    private JPanel createJobDisplayArea() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BorderLayout());
        String[] columnNames = {"Employer", "Title", "Company", "Location", "Max Applicants", "Salary", "Description", "Status"};

        // Create a custom table model to disable editing the "Employer" column
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Disable editing for the "Employer" column (index 0)
                if (column == 0) {
                    return false;
                }
                return super.isCellEditable(row, column);
            }
        };

        jobTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(jobTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JButton createButton(String text, int x, int y, JPanel panel) {
        JButton button = new JButton(text);
        button.setBounds(x, y, 150, 26);
        panel.add(button);
        return button;
    }

    public JButton getAddJobButton() {
        return addJobButton;
    }

    public JButton getUpdateJobButton() {
        return updateJobButton;
    }

    public JButton getDeleteJobButton() {
        return deleteJobButton;
    }

    public JButton getViewActiveJobsButton() {
        return viewActiveJobsButton;
    }

    public JButton getViewClosedJobsButton() {
        return viewClosedJobsButton;
    }

    public JButton getViewApplicantsButton() {
        return viewApplicantsButton;
    }

    public JButton getLogoutButton() {
        return logoutButton;
    }

    public JTable getJobTable() {
        return jobTable;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    // Method to update the relevant field in the JSON file
    private void updateJobFieldInJson(String jobTitle, String companyName, int column, String newValue) {
        try {
            // Path to the JSON file
            File file = new File("res/JobPosts.json");
            if (!file.exists()) {
                JOptionPane.showMessageDialog(this, "Job history file not found!", "Error", JOptionPane.ERROR_MESSAGE);
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

                    for (JsonElement jobElement : jobsArray) {
                        JsonObject job = jobElement.getAsJsonObject();
                        if (job.get("Title").getAsString().equals(jobTitle) &&
                                job.get("CompanyName").getAsString().equals(companyName)) {

                            // Update the field based on the column index
                            switch (column) {
                                case 1:
                                    job.addProperty("Title", newValue);
                                    break;
                                case 2:
                                    job.addProperty("CompanyName", newValue);
                                    break;
                                case 3:
                                    job.addProperty("JobLocation", newValue);
                                    break;
                                case 4:
                                    job.addProperty("MaxApplicants", Integer.parseInt(newValue));
                                    break;
                                case 5:
                                    job.addProperty("Salary", Integer.parseInt(newValue));
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
                            JOptionPane.showMessageDialog(this, "Job updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(this, "Error saving the updated job!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Job not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error updating the job!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}