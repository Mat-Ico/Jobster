package jobfinder.controller;

import server.ServerInterface;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import jobfinder.view.EmployeeDashboard;
import login.LogInController;
import login.LogInView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

public class EmployeeDashboardController {
    private EmployeeDashboard view;
    private final ServerInterface server;
    private String username;

    public EmployeeDashboardController(EmployeeDashboard view, ServerInterface server, String username) {
        this.view = view;
        this.server = server;
        this.username = username;
        initialize();
        fetchJobListings();
    }

    private void initialize() {
        view.getDashboardButton().addActionListener(e -> {
            fetchJobListings();
            view.showJobsCard();
        });

        view.getJobHistoryButton().addActionListener(e -> {
            showJobHistory();
            view.showJobsCard();
        });

        view.getEditAccountButton().addActionListener(e -> editAccount());

        view.getLogOutButton().addActionListener(e -> logout());

        view.getSearchField().addActionListener(e -> searchJobs());
    }

    private void fetchJobListings() {
        try {
            String jobsJson = server.getJobs();

            if (jobsJson.equals("No jobs available")) {
                JOptionPane.showMessageDialog(view.getFrame(), "No jobs available", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JsonArray jobArray = JsonParser.parseString(jobsJson).getAsJsonArray();
            String[][] jobData = new String[jobArray.size()][];

            for (int i = 0; i < jobArray.size(); i++) {
                JsonObject job = jobArray.get(i).getAsJsonObject();
                jobData[i] = new String[]{
                        job.get("Title").getAsString(),
                        job.get("CompanyName").getAsString(),
                        job.get("JobLocation").getAsString(),
                        job.get("MaxApplicants").getAsString(),
                        job.get("Salary").getAsString(),
                        job.get("Description").getAsString(),
                        job.get("Status").getAsString()
                };
            }

            SwingUtilities.invokeLater(() -> view.setJobTabs(jobData, new ApplyJobListener()));

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(view.getFrame(), "Error fetching jobs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (JsonSyntaxException e) {
            JOptionPane.showMessageDialog(view.getFrame(), "Error parsing job data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class ApplyJobListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String Title = e.getActionCommand();
            try {
                String response = server.applyForJob(username, Title);
                JOptionPane.showMessageDialog(view.getFrame(), response, "Application Status", JOptionPane.INFORMATION_MESSAGE);

                // Check if the application was rejected, allowing reapplication
                if (response.equals("Application rejected")) {
                    int reapply = JOptionPane.showConfirmDialog(view.getFrame(),
                            "Your application was rejected. Would you like to reapply?",
                            "Reapply?", JOptionPane.YES_NO_OPTION);

                    if (reapply == JOptionPane.YES_OPTION) {
                        // Retry applying for the job
                        String reapplyResponse = server.applyForJob(username, Title);
                        JOptionPane.showMessageDialog(view.getFrame(), reapplyResponse, "Reapplication Status", JOptionPane.INFORMATION_MESSAGE);
                    }
                }

                fetchJobListings();
                view.showJobsCard();

            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(view.getFrame(), "Failed to apply for job: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class CancelJobListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String Title = e.getActionCommand();
            try {
                String response = server.cancelJobApplication(username, Title);
                JOptionPane.showMessageDialog(view.getFrame(), response, "Cancellation Status", JOptionPane.INFORMATION_MESSAGE);
                showJobHistory();
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(view.getFrame(), "Failed to cancel job: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void showJobHistory() {
        try {
            // Fetch job history from the server
            String jobHistoryJson = server.getJobHistory(username);

            // Check if the response is empty or equals "[]"
            if (jobHistoryJson == null || jobHistoryJson.trim().isEmpty() || jobHistoryJson.equals("[]")) {
                JOptionPane.showMessageDialog(view.getFrame(), "No job history available", "Job History", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JsonArray jobArray;
            try {
                jobArray = JsonParser.parseString(jobHistoryJson).getAsJsonArray();
            } catch (JsonSyntaxException e) {
                JOptionPane.showMessageDialog(view.getFrame(), "Error parsing job history data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if the parsed array is empty
            if (jobArray.size() == 0) {
                JOptionPane.showMessageDialog(view.getFrame(), "No job history available", "Job History", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Prepare job history data for the table
            String[][] jobHistoryData = new String[jobArray.size()][5];
            for (int i = 0; i < jobArray.size(); i++) {
                JsonObject job = jobArray.get(i).getAsJsonObject();
                jobHistoryData[i] = new String[]{
                        job.has("Title") ? job.get("Title").getAsString() : "N/A",
                        job.has("CompanyName") ? job.get("CompanyName").getAsString() : "N/A",
                        job.has("JobLocation") ? job.get("JobLocation").getAsString() : "N/A",
                        job.has("Salary") ? String.valueOf(job.get("Salary").getAsInt()) : "N/A",
                        job.has("Status") ? job.get("Status").getAsString() : "N/A"
                };
            }

            // Update the UI with the job history data
            SwingUtilities.invokeLater(() -> {
                JFrame historyFrame = new JFrame("Job History");
                historyFrame.setSize(600, 400);
                historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                String[] columnNames = {"Title", "Company", "Location", "Salary", "Status"};
                JTable jobHistoryTable = new JTable(jobHistoryData, columnNames);
                JScrollPane scrollPane = new JScrollPane(jobHistoryTable);

                historyFrame.add(scrollPane);
                historyFrame.setVisible(true);
            });

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(view.getFrame(), "Error fetching job history: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editAccount() {
        view.showEditAccountCard();
        view.getEditUsernameField().setText(username);

        // Remove existing action listeners
        for (ActionListener al : view.getEditSaveButton().getActionListeners()) {
            view.getEditSaveButton().removeActionListener(al);
        }
        for (ActionListener al : view.getEditDeleteButton().getActionListeners()) {
            view.getEditDeleteButton().removeActionListener(al);
        }

        // Add action listener for saving the updated account
        view.getEditSaveButton().addActionListener(e -> {
            String newUsername = view.getEditUsernameField().getText().trim();
            String newPassword = new String(view.getEditPasswordField().getPassword()).trim();

            if (newUsername.isEmpty() || newPassword.isEmpty()) {
                showErrorMessage("Fields cannot be empty!");
                return;
            }

            try {
                // Update both username and password on the server
                String result = server.updateCredentials(username, newUsername, newPassword);

                if (result.equals("User not found.")) {
                    showErrorMessage("Update failed. User not found.");
                } else if (result.equals("Username already exists.")) {
                    showErrorMessage("Update failed. Username already taken.");
                } else {
                    showSuccessMessage("Account updated successfully!");
                    username = newUsername;  // Update the local username
                }
            } catch (RemoteException ex) {
                showErrorDialog("Error updating account: " + ex.getMessage());
            }
        });

        // Add action listener for deleting the account
        view.getEditDeleteButton().addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(view.getFrame(),
                    "Are you sure you want to delete your account?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    String result = server.deleteAccount(username);

                    if (result.equals("User not found.")) {
                        showErrorDialog("Failed to delete account. User not found.");
                    } else {
                        JOptionPane.showMessageDialog(view.getFrame(),
                                "Your account has been deleted.",
                                "Account Deleted", JOptionPane.INFORMATION_MESSAGE);
                        view.getFrame().dispose();

                        login.LogInController loginController = new login.LogInController(server);
                        loginController.view.getFrame().setVisible(true);
                    }
                } catch (RemoteException ex) {
                    showErrorDialog("Error deleting account: " + ex.getMessage());
                }
            }
        });
    }

    private void showErrorMessage(String message) {
        view.getEditErrorMessage().setText(message);
        view.getEditErrorMessage().setVisible(true);
        view.getEditSuccessMessage().setVisible(false);
    }

    private void showSuccessMessage(String message) {
        view.getEditSuccessMessage().setText(message);
        view.getEditSuccessMessage().setVisible(true);
        view.getEditErrorMessage().setVisible(false);
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(view.getFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);
    }


    private void logout() {
        JOptionPane.showMessageDialog(null, "Logging out...", "Logout", JOptionPane.INFORMATION_MESSAGE);
        view.getFrame().dispose();
        login.LogInController loginController = new LogInController(server);
        loginController.view.getFrame().setVisible(true);
    }

    private void searchJobs() {
        String query = view.getSearchField().getText().trim();

        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a search term.", "Search Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String response = server.searchJobs(query);

            if (response.equals("No jobs found")) {
                JOptionPane.showMessageDialog(view.getFrame(), "No matching jobs found", "Search Results", JOptionPane.ERROR_MESSAGE);
                fetchJobListings();
                return;
            }

            String[] jobLines = response.split("\n");
            String[][] jobs = new String[jobLines.length][];

            for (int i = 0; i < jobLines.length; i++) {
                jobs[i] = jobLines[i].split("\\|");
            }

            SwingUtilities.invokeLater(() -> view.setJobTabs(jobs, new ApplyJobListener()));

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(view.getFrame(), "Error searching for jobs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}