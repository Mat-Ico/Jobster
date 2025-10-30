package employer;

import com.google.gson.stream.JsonReader;
import server.ServerInterface;
import com.google.gson.*;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.rmi.RemoteException;

public class EmployerDashboardController {
    private EmployerDashboard view;
    private ServerInterface server;
    private String username;

    public EmployerDashboardController(EmployerDashboard view, ServerInterface server, String username) {
        this.view = view;
        this.server = server;
        this.username = username;
        addEventListeners();
        loadUserJobs();
    }

    private void addEventListeners() {
        view.getAddJobButton().addActionListener(e -> addJob());
        view.getUpdateJobButton().addActionListener(e -> updateJob());
        view.getDeleteJobButton().addActionListener(e -> deleteJob());
        view.getViewActiveJobsButton().addActionListener(e -> viewActiveJobs());
        view.getViewClosedJobsButton().addActionListener(e -> viewClosedJobs());
        view.getSearchButton().addActionListener(e -> searchJobs());
        view.getLogoutButton().addActionListener(e -> logout());
        view.getViewApplicantsButton().addActionListener(e -> viewApplicants());
    }

    private void addJob() {
        String title = JOptionPane.showInputDialog(view, "Enter Job Title:");
        if (title == null || title.trim().isEmpty()) return;

        String description = JOptionPane.showInputDialog(view, "Enter Job Description:");
        if (description == null || description.trim().isEmpty()) return;

        String location = JOptionPane.showInputDialog(view, "Enter Job Location:");
        if (location == null || location.trim().isEmpty()) return;

        String companyName = JOptionPane.showInputDialog(view, "Enter Company Name:");
        if (companyName == null || companyName.trim().isEmpty()) return;

        try {
            String maxParticipantsStr = JOptionPane.showInputDialog(view, "Enter Max Participants:");
            int maxParticipants = Integer.parseInt(maxParticipantsStr);

            String salaryStr = JOptionPane.showInputDialog(view, "Enter Job Salary:");
            int salary = Integer.parseInt(salaryStr);

            String response = server.addJob(username,title, description, location, companyName, maxParticipants, salary);
            JOptionPane.showMessageDialog(view, response, "Job Status", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException | RemoteException e) {
            JOptionPane.showMessageDialog(view, "Invalid input or server error!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchJobs() {
        try {
            String query = view.getSearchField().getText().trim();
            if (query.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Please enter a search term!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String jobsResponse = server.searchJobEmployer(username, query);
            DefaultTableModel model = view.getTableModel();
            model.setRowCount(0);

            if ("No jobs found".equals(jobsResponse)) {
                // Show a pop-up when no jobs are found
                JOptionPane.showMessageDialog(view, "No jobs found", "No Results", JOptionPane.INFORMATION_MESSAGE);
            } else {
                try {
                    JsonArray jobsArray = JsonParser.parseString(jobsResponse).getAsJsonArray();
                    for (JsonElement jobElement : jobsArray) {
                        JsonObject job = jobElement.getAsJsonObject();
                        model.addRow(new Object[]{
                                job.get("Employer").getAsString(),
                                job.get("Title").getAsString(),
                                job.get("CompanyName").getAsString(),
                                job.get("JobLocation").getAsString(),
                                job.get("MaxApplicants").getAsInt(),
                                job.get("Salary").getAsInt(),
                                job.get("Description").getAsString(),
                                job.get("Status").getAsString()
                        });
                    }
                } catch (JsonSyntaxException | IllegalStateException e) {
                    JOptionPane.showMessageDialog(view, "Error parsing job data!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(view, "Error retrieving jobs!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateJob() {
        try {
            int selectedRow = view.getJobTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view, "Please select a job to update.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Retrieve current job details from the selected row
            String jobTitle = view.getJobTable().getValueAt(selectedRow, 1).toString();
            String companyName = view.getJobTable().getValueAt(selectedRow, 2).toString();

            // Fetch new values directly from table cells
            String newTitle = (String) view.getJobTable().getValueAt(selectedRow, 1);
            String newCompanyName = (String) view.getJobTable().getValueAt(selectedRow, 2);
            String newLocation = (String) view.getJobTable().getValueAt(selectedRow, 3);
            String newSalary = (String) view.getJobTable().getValueAt(selectedRow, 4);
            String newMaxApplicants = (String) view.getJobTable().getValueAt(selectedRow, 5);

            // Load the JSON data
            File file = new File("res/JobPosts.json");
            if (!file.exists()) {
                JOptionPane.showMessageDialog(view, "Job history file not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            FileReader reader = new FileReader(file);
            JsonElement parsedElement = JsonParser.parseReader(reader);
            reader.close();

            if (parsedElement.isJsonObject()) {
                JsonObject rootObject = parsedElement.getAsJsonObject();
                if (rootObject.has("Job") && rootObject.get("Job").isJsonArray()) {
                    JsonArray jobsArray = rootObject.getAsJsonArray("Job");
                    boolean jobUpdated = false;

                    for (JsonElement jobElement : jobsArray) {
                        JsonObject job = jobElement.getAsJsonObject();
                        if (job.get("Title").getAsString().equals(jobTitle) &&
                                job.get("CompanyName").getAsString().equals(companyName)) {

                            // Update only the fields that are not blank (update table data as well)
                            if (!newTitle.trim().isEmpty()) {
                                job.addProperty("Title", newTitle);
                                view.getJobTable().setValueAt(newTitle, selectedRow, 1); // Update table cell
                            }
                            if (!newCompanyName.trim().isEmpty()) {
                                job.addProperty("CompanyName", newCompanyName);
                                view.getJobTable().setValueAt(newCompanyName, selectedRow, 2); // Update table cell
                            }
                            if (!newLocation.trim().isEmpty()) {
                                job.addProperty("JobLocation", newLocation);
                                view.getJobTable().setValueAt(newLocation, selectedRow, 3); // Update table cell
                            }
                            if (!newSalary.trim().isEmpty()) {
                                try {
                                    int salary = Integer.parseInt(newSalary);
                                    job.addProperty("Salary", salary);
                                    view.getJobTable().setValueAt(newSalary, selectedRow, 4); // Update table cell
                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(view, "Invalid salary input! Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }
                            if (!newMaxApplicants.trim().isEmpty()) {
                                try {
                                    int maxApplicants = Integer.parseInt(newMaxApplicants);
                                    job.addProperty("MaxApplicants", maxApplicants);
                                    view.getJobTable().setValueAt(newMaxApplicants, selectedRow, 5); // Update table cell
                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(view, "Invalid max applicants input! Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }
                            jobUpdated = true;
                            break;
                        }
                    }

                    if (jobUpdated) {
                        try (FileWriter writer = new FileWriter("res/JobPosts.json")) {
                            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(rootObject));
                            JOptionPane.showMessageDialog(view, "Job updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(view, "Error saving the updated job!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(view, "Job not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "Error updating the job!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteJob() {
        try {
            int selectedRow = view.getJobTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view, "Please select a job to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String jobTitle = view.getJobTable().getValueAt(selectedRow, 1).toString();
            String companyName = view.getJobTable().getValueAt(selectedRow, 2).toString();

            int confirm = JOptionPane.showConfirmDialog(view,
                    "Are you sure you want to delete the job: " + jobTitle + " from " + companyName + "?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Delete job from the server
                String response = server.deleteJob(username, jobTitle, companyName);
                JOptionPane.showMessageDialog(view, response, "Delete Status", JOptionPane.INFORMATION_MESSAGE);

                // Update the JSON file
                updateJobInJson(jobTitle, companyName);

                // Refresh the table
                ((DefaultTableModel) view.getJobTable().getModel()).removeRow(selectedRow);
            }

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(view, "Server error!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateJobInJson(String jobTitle, String companyName) {
        try {
            // Read the JSON file
            FileReader reader = new FileReader("res/JobPosts.json");
            JsonElement parsedElement = JsonParser.parseReader(reader);
            reader.close();

            if (parsedElement.isJsonObject()) {
                JsonObject rootObject = parsedElement.getAsJsonObject();
                if (rootObject.has("Job") && rootObject.get("Job").isJsonArray()) {
                    JsonArray jobsArray = rootObject.getAsJsonArray("Job");

                    // Iterate and remove the matching job
                    for (int i = 0; i < jobsArray.size(); i++) {
                        JsonObject job = jobsArray.get(i).getAsJsonObject();
                        if (job.has("Title") && job.has("CompanyName") &&
                                job.get("Title").getAsString().equals(jobTitle) &&
                                job.get("CompanyName").getAsString().equals(companyName)) {
                            jobsArray.remove(i);
                            break;
                        }
                    }

                    // Write the updated JSON back to the file
                    FileWriter writer = new FileWriter("res/JobPosts.json");
                    writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(rootObject));
                    writer.close();
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "Error updating the JSON file!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void viewActiveJobs() {
        try {
            String jobs = server.getActiveJobs(username);
            DefaultTableModel model = view.getTableModel();
            model.setRowCount(0);
            if ("No active jobs available.".equals(jobs)) {
                model.addRow(new Object[]{"No jobs found", "", "", "", "", "", "", ""});
            } else {
                try {
                    JsonArray jobsArray = JsonParser.parseString(jobs).getAsJsonArray();
                    for (JsonElement jobElement : jobsArray) {
                        JsonObject job = jobElement.getAsJsonObject();
                        model.addRow(new Object[]{
                                job.get("Employer").getAsString(),
                                job.get("Title").getAsString(),
                                job.get("CompanyName").getAsString(),
                                job.get("JobLocation").getAsString(),
                                job.get("MaxApplicants").getAsString(),
                                job.get("Salary").getAsString(),
                                job.get("Description").getAsString(),
                                job.get("Status").getAsString()
                        });
                    }
                } catch (JsonSyntaxException | IllegalStateException e) {
                    JOptionPane.showMessageDialog(view, jobs, "Active Jobs", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(view, "Error retrieving jobs!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void loadUserJobs() {
        try {
            String jobs = server.getUserJobs(username);
            DefaultTableModel model = view.getTableModel();
            model.setRowCount(0);

            if ("No jobs found.".equals(jobs)) {
                model.addRow(new Object[]{"No jobs found", "", "", "", "", "", "", ""});
            } else {
                try {
                    JsonArray jobsArray = JsonParser.parseString(jobs).getAsJsonArray();

                    for (JsonElement jobElement : jobsArray) {
                        JsonObject job = jobElement.getAsJsonObject();
                        model.addRow(new Object[]{
                                job.get("Employer").getAsString(),
                                job.get("Title").getAsString(),
                                job.get("CompanyName").getAsString(),
                                job.get("JobLocation").getAsString(),
                                job.get("MaxApplicants").getAsString(),
                                job.get("Salary").getAsString(),
                                job.get("Description").getAsString(),
                                job.get("Status").getAsString()
                        });
                    }
                } catch (JsonSyntaxException | IllegalStateException e) {
                    JOptionPane.showMessageDialog(view, jobs, "Your Jobs", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(view, "Error retrieving jobs!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewClosedJobs() {
        try {
            String jobs = server.getClosedJobs(username);
            DefaultTableModel model = view.getTableModel();
            model.setRowCount(0);
            if ("No closed jobs available.".equals(jobs)) {
                model.addRow(new Object[]{"No jobs found", "", "", "", "", "", "", ""});
            } else {
                try {
                    JsonArray jobsArray = JsonParser.parseString(jobs).getAsJsonArray();
                    for (JsonElement jobElement : jobsArray) {
                        JsonObject job = jobElement.getAsJsonObject();
                        model.addRow(new Object[]{
                                job.get("Employer").getAsString(),
                                job.get("Title").getAsString(),
                                job.get("CompanyName").getAsString(),
                                job.get("JobLocation").getAsString(),
                                job.get("MaxApplicants").getAsString(),
                                job.get("Salary").getAsString(),
                                job.get("Description").getAsString(),
                                job.get("Status").getAsString()
                        });
                    }
                } catch (JsonSyntaxException | IllegalStateException e) {
                    JOptionPane.showMessageDialog(view, jobs, "Closed Jobs", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(view, "Error retrieving jobs!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        view.dispose();
        JOptionPane.showMessageDialog(null, "Logged out successfully!", "Logout", JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewApplicants() {
        try {
            int selectedRow = view.getJobTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view, "Please select a job to view applicants.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String jobTitle = view.getJobTable().getValueAt(selectedRow, 1).toString();
            String companyName = view.getJobTable().getValueAt(selectedRow, 2).toString();

            // Fetch applicants from the server
            String applicantsResponse = server.getJobApplicants(username, jobTitle, companyName);

            if (applicantsResponse == null || applicantsResponse.isEmpty()) {
                JOptionPane.showMessageDialog(view, "No applicants available for this job.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JsonArray applicantsArray = new JsonArray();
            try {
                JsonElement parsedElement = JsonParser.parseString(applicantsResponse);

                if (parsedElement.isJsonObject()) {
                    JsonObject responseObject = parsedElement.getAsJsonObject();

                    // Debugging: Check keys in the JSON object
                    System.out.println("Available Keys: " + responseObject.keySet());

                    if (responseObject.has("JobHistory") && responseObject.get("JobHistory").isJsonArray()) {
                        JsonArray jobHistoryArray = responseObject.getAsJsonArray("JobHistory");

                        // Filter applicants based on job title and company name
                        for (JsonElement applicantElement : jobHistoryArray) {
                            JsonObject applicant = applicantElement.getAsJsonObject();
                            String title = applicant.has("Title") ? applicant.get("Title").getAsString() : "";
                            String company = applicant.has("CompanyName") ? applicant.get("CompanyName").getAsString() : "";

                            if (jobTitle.equals(title) && companyName.equals(company)) {
                                applicantsArray.add(applicant);
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(view, "Unexpected response format. Available keys: " + responseObject.keySet(), "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(view, "Error: Response is not a JSON object.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (JsonSyntaxException | IllegalStateException e) {
                JOptionPane.showMessageDialog(view, "Error parsing applicant data!", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return;
            }

            if (applicantsArray.size() == 0) {
                JOptionPane.showMessageDialog(view, "No applicants available for this job.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Create a new frame for displaying applicants in a table
            JFrame applicantFrame = new JFrame("Applicants for " + jobTitle);
            applicantFrame.setSize(600, 400);
            applicantFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            applicantFrame.setLayout(new BorderLayout());

            // Applicant table setup with an additional "Job Title" column
            String[] columnNames = {"Username", "Job Title", "Status"};
            DefaultTableModel applicantTableModel = new DefaultTableModel(columnNames, 0);
            JTable applicantTable = new JTable(applicantTableModel);

            // Populate the table with applicant data
            for (JsonElement applicantElement : applicantsArray) {
                JsonObject applicant = applicantElement.getAsJsonObject();
                String applicantName = applicant.has("username") ? applicant.get("username").getAsString() : "Unknown";
                String title = applicant.has("Title") ? applicant.get("Title").getAsString() : "Unknown";
                String status = applicant.has("Status") ? applicant.get("Status").getAsString() : "N/A";
                applicantTableModel.addRow(new Object[]{applicantName, title, status});
            }

            // Button panel
            JPanel buttonPanel = new JPanel();
            JButton acceptButton = new JButton("Accept");
            JButton rejectButton = new JButton("Reject");

            // Add buttons to panel
            buttonPanel.add(acceptButton);
            buttonPanel.add(rejectButton);

            // Button actions
            acceptButton.addActionListener(e -> handleApplicantAction(applicantTable, applicantTableModel, jobTitle, companyName, "Accept", "Accepted"));
            rejectButton.addActionListener(e -> handleApplicantAction(applicantTable, applicantTableModel, jobTitle, companyName, "Reject", "Rejected"));

            // Add the table to a scroll pane and set it in the frame
            JScrollPane scrollPane = new JScrollPane(applicantTable);
            applicantFrame.add(scrollPane, BorderLayout.CENTER);
            applicantFrame.add(buttonPanel, BorderLayout.SOUTH);
            applicantFrame.setVisible(true);

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(view, "Connection error while retrieving applicants.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void handleApplicantAction(JTable applicantTable, DefaultTableModel model, String jobTitle, String companyName, String action, String newStatus) {
        int selectedRow = applicantTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select an applicant.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String applicantUsername = model.getValueAt(selectedRow, 0).toString();

        try {
            boolean success = Boolean.parseBoolean(server.updateApplicantStatus(applicantUsername, jobTitle, companyName, action)); // This should handle the update on server

            if (success) {
                model.setValueAt(newStatus, selectedRow, 2); // Update status in table
                JOptionPane.showMessageDialog(null, "Applicant " + action + "ed successfully.");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to " + action + " applicant.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Server error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateApplicantStatusInJson(String applicantName, String jobTitle, String companyName, String newStatus) {
        try {
            // Read the JSON file
            FileReader reader = new FileReader("res/JobHistory.json");
            JsonElement parsedElement = JsonParser.parseReader(reader);
            reader.close();

            // Check if the root element is an object and contains the "JobHistory" key
            if (parsedElement.isJsonObject()) {
                JsonObject rootObject = parsedElement.getAsJsonObject();

                if (rootObject.has("JobHistory") && rootObject.get("JobHistory").isJsonArray()) {
                    JsonArray applicantsArray = rootObject.getAsJsonArray("JobHistory");

                    // Update the status of the applicant in the array
                    for (JsonElement applicantElement : applicantsArray) {
                        JsonObject applicant = applicantElement.getAsJsonObject();
                        if (applicant.has("username") && applicant.has("Title") && applicant.has("CompanyName") &&
                                applicant.get("username").getAsString().equals(applicantName) &&
                                applicant.get("Title").getAsString().equals(jobTitle) &&
                                applicant.get("CompanyName").getAsString().equals(companyName)) {
                            applicant.addProperty("Status", newStatus);
                        }
                    }

                    // Write the updated JSON back to the file
                    FileWriter writer = new FileWriter("res/JobHistory.json");
                    writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(rootObject));
                    writer.close();
                    System.out.println("Updated JSON file successfully.");
                } else {
                    System.err.println("Error: 'JobHistory' key is missing or not an array.");
                }
            } else {
                System.err.println("Error: Root element is not a JSON object.");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "Error updating the JSON file!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
