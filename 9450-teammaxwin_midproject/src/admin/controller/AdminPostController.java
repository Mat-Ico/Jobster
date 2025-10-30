package admin.controller;

import admin.view.AdminDashboardView;
import admin.view.AdminPostView;
import com.google.gson.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;

public class AdminPostController {
    private static final String JOB_DATA_FILE = "res/JobPosts.json";
    private final AdminPostView view;
    private JsonArray jobsArray;
    public static boolean isPostViewOpen = false;

    public AdminPostController(AdminPostView view, AdminDashboardController dashboardController) {
        this.view = view;
        isPostViewOpen = true;
        loadJobs();
        initializeListeners();
    }

    private void loadJobs() {
        jobsArray = new JsonArray();
        try {
            File file = new File(JOB_DATA_FILE);
            if (!file.exists()) {
                saveJobs(); // Create file if not exists
            }

            JsonObject data = new Gson().fromJson(new FileReader(file), JsonObject.class);
            if (data != null && data.has("Jobs")) {
                jobsArray = data.getAsJsonArray("Jobs");
            }
            updatePostTable();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(view.getFrame(), "Error loading job data!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (JsonSyntaxException e) {
            JOptionPane.showMessageDialog(view.getFrame(), "Invalid JSON format in the job data file!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePostTable() {
        DefaultTableModel model = (DefaultTableModel) view.getPostTable().getModel();
        model.setRowCount(0);

        for (JsonElement jobElement : jobsArray) {
            JsonObject job = jobElement.getAsJsonObject();
            model.addRow(new Object[] {
                    job.get("Title").getAsString(),
                    job.get("CompanyName").getAsString(),
                    job.get("JobLocation").getAsString(),
                    job.get("MaxApplicants").getAsInt(),
                    job.get("Salary").getAsInt(),
                    job.get("Description").getAsString(),
                    job.get("Status").getAsString(),
                    job.has("Employer") ? job.get("Employer").getAsString() : ""
            });
        }
    }

    private void initializeListeners() {
        view.addDeleteListener(e -> deletePost());
        view.addSearchButtonListener(e -> searchPosts());
        view.addRefreshListener(e -> refreshPosts());
        view.addBackListener(e -> backToDashboard());
        view.addAddPostListener(e -> addPost()); // Add listener for Add Post
        view.addEditPostListener(e -> editPost()); // Add listener for Edit Post
    }

    private void deletePost() {
        int selectedRow = view.getSelectedRowIndex();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view.getPostTable(), "Please select a post to delete!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String Title = view.getSelectedTitle();
        int confirm = JOptionPane.showConfirmDialog(view.getPostTable(),
                "Delete post '" + Title + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            jobsArray.remove(selectedRow);
            saveJobs();
            updatePostTable();
        }
    }

    private void addPost() {
        JsonObject job = new JsonObject();

        try {
            String employer = JOptionPane.showInputDialog("Enter Employer Name or ID:");

            // If the user cancels the input (employer is null), exit the method.
            if (employer == null || employer.trim().isEmpty()) {
                JOptionPane.showMessageDialog(view.getPostTable(), "Employer name is required. Post not added.", "Error", JOptionPane.ERROR_MESSAGE);
                return; // Prevent further processing and close the dialog
            }

            if (!isEmployerValid(employer)) {
                JOptionPane.showMessageDialog(view.getPostTable(), "Employer not found! Please provide a valid employer name.", "Error", JOptionPane.ERROR_MESSAGE);
                return; // Stop the process if the employer is invalid
            }

            job.addProperty("Title", JOptionPane.showInputDialog("Enter Job Title:"));
            job.addProperty("CompanyName", JOptionPane.showInputDialog("Enter Company Name:"));
            job.addProperty("JobLocation", JOptionPane.showInputDialog("Enter Job Location:"));
            job.addProperty("MaxApplicants", Integer.parseInt(JOptionPane.showInputDialog("Enter Max Applicants:")));
            job.addProperty("Salary", Integer.parseInt(JOptionPane.showInputDialog("Enter Salary:")));
            job.addProperty("Description", JOptionPane.showInputDialog("Enter Description:"));
            job.addProperty("Status", JOptionPane.showInputDialog("Enter Status (Open/Closed):"));
            job.addProperty("Employer", employer);

            jobsArray.add(job);
            saveJobs();
            updatePostTable();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view.getPostTable(), "Failed to add post. Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editPost() {
        int selectedRow = view.getSelectedRowIndex();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view.getPostTable(), "Please select a post to edit!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the currently selected job post
        JsonObject job = jobsArray.get(selectedRow).getAsJsonObject();

        // Update the job object with the new values directly from the table cells
        job.addProperty("Title", view.getPostTable().getValueAt(selectedRow, 0).toString());
        job.addProperty("CompanyName", view.getPostTable().getValueAt(selectedRow, 1).toString());
        job.addProperty("JobLocation", view.getPostTable().getValueAt(selectedRow, 2).toString());
        job.addProperty("MaxApplicants", Integer.parseInt(view.getPostTable().getValueAt(selectedRow, 3).toString()));
        job.addProperty("Salary", Integer.parseInt(view.getPostTable().getValueAt(selectedRow, 4).toString()));
        job.addProperty("Description", view.getPostTable().getValueAt(selectedRow, 5).toString());
        job.addProperty("Status", view.getPostTable().getValueAt(selectedRow, 6).toString());
        job.addProperty("Employer", view.getPostTable().getValueAt(selectedRow, 7).toString());

        // Save the updated job list
        saveJobs();

        // Refresh the table to reflect changes
        updatePostTable();
    }

    // This method checks if the employer name exists in the list of job posts.
    private boolean isEmployerValid(String employerName) {
        for (JsonElement jobElement : jobsArray) {
            JsonObject job = jobElement.getAsJsonObject();
            String existingEmployer = job.has("Employer") ? job.get("Employer").getAsString() : "";
            if (existingEmployer.equalsIgnoreCase(employerName)) {
                return true; // Employer found
            }
        }
        return false; // Employer not found
    }

    private void searchPosts() {
        String query = view.getSearchText().toLowerCase();
        DefaultTableModel model = (DefaultTableModel) view.getPostTable().getModel();
        model.setRowCount(0);

        boolean resultsFound = false; // Flag to track if any results are found

        for (JsonElement jobElement : jobsArray) {
            JsonObject job = jobElement.getAsJsonObject();
            String title = job.get("Title").getAsString().toLowerCase();
            String company = job.get("CompanyName").getAsString().toLowerCase();
            String location = job.get("JobLocation").getAsString().toLowerCase();
            String employer = job.has("Employer") ? job.get("Employer").getAsString().toLowerCase() : "";

            if (title.contains(query) || company.contains(query) || location.contains(query) || employer.contains(query)) {
                model.addRow(new Object[] {
                        job.get("Title").getAsString(),
                        job.get("CompanyName").getAsString(),
                        job.get("JobLocation").getAsString(),
                        job.get("MaxApplicants").getAsInt(),
                        job.get("Salary").getAsInt(),
                        job.get("Description").getAsString(),
                        job.get("Status").getAsString(),
                        job.has("Employer") ? job.get("Employer").getAsString() : ""
                });
                resultsFound = true; // Mark that results were found
            }
        }

        if (!resultsFound) {
            JOptionPane.showMessageDialog(view, "No posts found", "No Results", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    private void refreshPosts() {
        loadJobs();
    }

    private void saveJobs() {
        try (Writer writer = new FileWriter(JOB_DATA_FILE)) {
            JsonObject root = new JsonObject();
            root.add("Jobs", jobsArray);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(root, writer);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view.getPostTable(), "Error saving job data!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void backToDashboard() {
        isPostViewOpen = false;
        view.close();
        new AdminDashboardController(new AdminDashboardView()).showAdminDashboard();
    }
}