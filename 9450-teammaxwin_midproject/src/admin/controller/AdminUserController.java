package admin.controller;

import admin.view.AdminDashboardView;
import admin.view.AdminUserView;
import com.google.gson.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;

public class AdminUserController {
    private static final String USER_DATA_FILE = "res/user.json";
    private final AdminUserView view;
    private JsonArray usersArray;

    public AdminUserController(AdminUserView view) {
        this.view = view;
        loadUsers();
        initializeListeners();
    }

    private JSONArray loadUsers() {
        try {
            JsonObject data = new Gson().fromJson(new FileReader(USER_DATA_FILE), JsonObject.class);
            usersArray = data.getAsJsonArray("users");
            updateUserTable();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(view.getUserTable(), "Error loading user data!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private void updateUserTable() {
        DefaultTableModel model = (DefaultTableModel) view.getUserTable().getModel();
        model.setRowCount(0);

        for (JsonElement userElement : usersArray) {
            JsonObject user = userElement.getAsJsonObject();
            model.addRow(new Object[]{
                    user.get("username").getAsString(),
                    user.get("password").getAsString(),
                    user.get("role").getAsString()
            });
        }
    }

    private void initializeListeners() {
        view.addAddUserListener(e -> addUser());
        view.addDeleteUserListener(e -> deleteUser());
        view.addEditUserListener(e -> editUser());
        view.addSearchButtonListener(e -> searchUsers());
        view.addRefreshListener(e -> refreshUsers());
        view.addBackListener(e -> backToDashboard());
    }

    private void addUser() {
        String username = JOptionPane.showInputDialog(view.getUserTable(), "Enter username:");
        if (username == null || username.isEmpty()) return;

        String password = JOptionPane.showInputDialog(view.getUserTable(), "Enter password:");
        if (password == null || password.isEmpty()) return;

        String role = (String) JOptionPane.showInputDialog(view.getUserTable(),
                "Select role:", "Role Selection",
                JOptionPane.QUESTION_MESSAGE, null,
                new String[]{"employer", "jobfinder"}, "employer");

        if (role == null) return;

        // Check if username exists
        for (JsonElement user : usersArray) {
            if (user.getAsJsonObject().get("username").getAsString().equalsIgnoreCase(username)) {
                JOptionPane.showMessageDialog(view.getUserTable(), "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Add new user
        JsonObject newUser = new JsonObject();
        newUser.addProperty("username", username);
        newUser.addProperty("password", password);
        newUser.addProperty("role", role);
        usersArray.add(newUser);

        saveUsers();
        updateUserTable();
    }

    private void deleteUser() {
        // Check if view is null
        if (view == null) {
            JOptionPane.showMessageDialog(null, "View is not initialized!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = view.getUserTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view.getUserTable(), "Please select a user to delete!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = view.getUsernameAtRow(selectedRow);
        String userType = view.getUserTypeAtRow(selectedRow);  // Assuming you have a way to get the user type

        int confirm = JOptionPane.showConfirmDialog(view.getUserTable(),
                "Delete user '" + username + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // If the user is an employer, delete all of their posts from the JSON file
            if ("employer".equals(userType)) {
                deleteEmployerPosts(username);  // Method to delete posts from the JSON file
            }

            // Remove the user from the list and save
            JsonArray updatedUsersArray = new JsonArray();
            for (int i = 0; i < usersArray.size(); i++) {
                JsonObject user = usersArray.get(i).getAsJsonObject();
                if (!user.get("username").getAsString().equals(username)) {
                    updatedUsersArray.add(user);  // Add the user to the updated array if it's not the one to delete
                }
            }

            // Replace the old usersArray with the updated one
            usersArray = updatedUsersArray;

            // Save the updated usersArray to the file and refresh the user table
            saveUsers();
            updateUserTable();
        }
    }
    private void deleteEmployerPosts(String username) {
        // Check if the user is an employer
        if (!isEmployer(username)) {
            System.out.println("User is not an employer: " + username);
            return; // If user is not an employer, exit
        }

        // Load the jobs from the JSON file
        JSONArray jobsArray = loadJobs();  // Load the jobs from the file

        // Debugging: Check the number of jobs before deletion
        System.out.println("Number of jobs before deletion: " + jobsArray.length());

        boolean foundMatch = false;  // Flag to track if any job posts are deleted

        // Iterate through the jobs and remove those posted by the employer
        for (int i = jobsArray.length() - 1; i >= 0; i--) {
            JSONObject job = jobsArray.getJSONObject(i);
            String employerName = job.getString("Employer");

            // Log the employer and check if it matches
            System.out.println("Checking job posted by: " + employerName);

            // Normalize case to avoid case mismatch
            if (username.equalsIgnoreCase(employerName)) {
                // Job matches the employer, delete it
                System.out.println("Deleting job: " + job.getString("Title") + " posted by " + employerName);
                jobsArray.remove(i);  // Remove the job if the employer matches
                foundMatch = true;
            }
        }

        // Check whether any posts were deleted
        if (foundMatch) {
            System.out.println("Some job posts were deleted.");
            // Save the updated jobs array back to the JSON file
            saveDataJobPost(jobsArray, "res/JobPosts.json");
        } else {
            System.out.println("No job posts were deleted for employer: " + username);
        }

        // Log the number of jobs after deletion
        System.out.println("Number of jobs after deletion: " + jobsArray.length());
    }

    private boolean isEmployer(String username) {
        // Load the users from the JSON file
        JSONArray usersArray = loadUsers(); // Load the users from the file

        // Debugging: Log the check for each user
        System.out.println("Checking users for employer: " + username);

        // Iterate over users to find an employer with the matching username
        for (int i = 0; i < usersArray.length(); i++) {
            JSONObject user = usersArray.getJSONObject(i);
            String storedUsername = user.getString("username");
            String role = user.getString("role");

            // Log user details to confirm they are being checked
            System.out.println("User: " + storedUsername + ", Role: " + role);

            // Check if the username matches and the user is an employer
            if (username.equalsIgnoreCase(storedUsername) && role.equals("employer")) {
                return true;  // Found the employer
            }
        }

        return false;  // No employer found with the given username
    }

    private JSONArray loadJobs() {
        // Load the jobs from the JSON file
        File jobsFile = new File("res/JobPosts.json");
        JSONArray jobsArray = new JSONArray();

        // If the file exists and is readable, load the jobs
        if (jobsFile.exists() && jobsFile.canRead()) {
            try (FileReader reader = new FileReader(jobsFile)) {
                // Parse the JSON file into a JSONArray
                JSONTokener tokener = new JSONTokener(reader);
                jobsArray = new JSONArray(tokener);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Job posts file is missing or unreadable.");
        }

        return jobsArray;
    }

    private void saveDataJobPost(JSONArray updatedData, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(updatedData.toString(), writer);  // Write the updated data to the file
            System.out.println("Job posts have been successfully saved.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editUser() {
        int selectedRow = view.getUserTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view.getUserTable(), "Please select a user to edit!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the selected user object from the array
        JsonObject user = usersArray.get(selectedRow).getAsJsonObject();

        // Prompt to edit the username
        String newUsername = JOptionPane.showInputDialog(view.getUserTable(), "Enter new username:", user.get("username").getAsString());
        if (newUsername == null || newUsername.isEmpty()) return;

        // Prompt to edit the password
        String newPassword = JOptionPane.showInputDialog(view.getUserTable(), "Enter new password:", user.get("password").getAsString());
        if (newPassword == null || newPassword.isEmpty()) return;

        // Prompt to edit the role
        String newRole = (String) JOptionPane.showInputDialog(view.getUserTable(),
                "Select new role:", "Role Selection",
                JOptionPane.QUESTION_MESSAGE, null,
                new String[]{"employer", "jobfinder"}, user.get("role").getAsString());
        if (newRole == null) return;

        // Update user properties
        user.addProperty("username", newUsername);
        user.addProperty("password", newPassword);
        user.addProperty("role", newRole);

        // Save the updated users to the JSON file and refresh the table
        saveUsers();
        updateUserTable();
        JOptionPane.showMessageDialog(view.getUserTable(), "User updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void searchUsers() {
        String query = view.getSearchText().toLowerCase();
        DefaultTableModel model = (DefaultTableModel) view.getUserTable().getModel();
        model.setRowCount(0);

        boolean resultsFound = false; // Flag to track if any results are found

        for (JsonElement userElement : usersArray) {
            JsonObject user = userElement.getAsJsonObject();
            String username = user.get("username").getAsString().toLowerCase();
            if (username.contains(query)) {
                model.addRow(new Object[]{
                        user.get("username").getAsString(),
                        user.get("password").getAsString(),
                        user.get("role").getAsString()
                });
                resultsFound = true; // Mark that results were found
            }
        }

        if (!resultsFound) {
            // Show a pop-up if no results were found
            JOptionPane.showMessageDialog(view.getUserTable(), "No users found", "No Results", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void refreshUsers() {
        loadUsers();
    }

    private void saveUsers() {
        try (Writer writer = new FileWriter(USER_DATA_FILE)) {
            JsonObject root = new JsonObject();
            root.add("users", usersArray);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(root, writer);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view.getUserTable(), "Error saving user data!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void backToDashboard() {
        view.close();
        new AdminDashboardController(new AdminDashboardView()).showAdminDashboard();
    }
}
