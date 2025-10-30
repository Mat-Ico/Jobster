package server;

import com.google.gson.*;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ServerImpl extends UnicastRemoteObject implements ServerInterface {
    private static final String USER_DATA_FILE = "res/user.json";
    private static final String JOB_DATA_FILE = "res/JobPosts.json";
    private static final String JOB_HISTORY_DATA_FILE = "res/JobHistory.json";
    private static final String SERVER_MESSAGE_LOG_FILE = "res/MessageFromServer.json";
    private boolean isServerRunning = false;
    private JsonObject jobData;

    public ServerImpl() throws RemoteException {
    }

    @Override
    public boolean login(String username, String password) throws RemoteException {
        try {
            // Load user data
            JsonObject data = loadData(USER_DATA_FILE);

            // Check if data is loaded correctly
            if (data == null || !data.has("users") || !data.get("users").isJsonArray()) {
                return false;
            }

            JsonArray users = data.getAsJsonArray("users");

            // Iterate over each user to check credentials
            for (JsonElement userElement : users) {
                if (!userElement.isJsonObject()) continue;

                JsonObject user = userElement.getAsJsonObject();
                String storedUsername = user.has("username") ? user.get("username").getAsString() : "";
                String storedPassword = user.has("password") ? user.get("password").getAsString() : "";
                String role = user.has("role") ? user.get("role").getAsString() : "Unknown";

                if (storedUsername.equals(username) && storedPassword.equals(password)) {
                    System.out.println("Logged in: " + storedUsername);
                    System.out.println("Role: " + role);
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String authenticate(String username, String password) throws RemoteException {
        JsonObject data = (JsonObject) loadData(USER_DATA_FILE);

        if (data != null && data.has("users")) {
            JsonArray users = data.getAsJsonArray("users");

            for (JsonElement userElement : users) {
                JsonObject user = userElement.getAsJsonObject();
                if (user.get("username").getAsString().equals(username) &&
                        user.get("password").getAsString().equals(password)) {
                    return user.get("role").getAsString();
                }
            }
        }
        return "Invalid username or password";
    }

    @Override
    public String getJobs() throws RemoteException {
        JsonObject data = (JsonObject) loadData(JOB_DATA_FILE);
        if (data != null && data.has("Jobs")) {
            return data.get("Jobs").toString();
        }
        return "No jobs available";
    }


    @Override
    public String updateJobStatus(String jobTitle, String status) throws RemoteException {
        JsonObject data = (JsonObject) loadData(jobTitle);
        if (data != null && data.has("Jobs")) {
            JsonArray jobs = data.getAsJsonArray("Jobs");
            for (JsonElement jobElement : jobs) {
                JsonObject job = jobElement.getAsJsonObject();
                if (job.get("Title").getAsString().equals(jobTitle)) {
                    job.addProperty("Status", status);
                    saveDataJobHistory(data,JOB_HISTORY_DATA_FILE);
                    return "Job status updated";
                }
            }
        }
        return "Job not found";
    }

    @Override
    public String applyForJob(String username, String jobTitle) throws RemoteException {
        System.out.println(username + " is applying for the job: " + jobTitle);
        logServerMessage(username, "ApplyForJob", "Attempting to apply for " + jobTitle);

        JsonObject jobHistoryData = loadData(JOB_HISTORY_DATA_FILE);

        // Initialize JobHistory if missing
        if (jobHistoryData == null || !jobHistoryData.has("JobHistory")) {
            jobHistoryData = new JsonObject();
            jobHistoryData.add("JobHistory", new JsonArray());
        }

        JsonArray jobHistory = jobHistoryData.getAsJsonArray("JobHistory");

        // Check if the user already applied for this job
        for (JsonElement jobElement : jobHistory) {
            JsonObject job = jobElement.getAsJsonObject();
            if (job.get("Title").getAsString().equals(jobTitle) && job.get("username").getAsString().equals(username)) {
                String status = job.get("Status").getAsString();

                // If previously rejected, allow reapplication
                if (status.equals("Rejected")) {
                    System.out.println(username + " was previously rejected for: " + jobTitle);
                    logServerMessage(username, "ApplyForJob", "Reapplying for " + jobTitle);

                    // Reset status to "Pending"
                    job.addProperty("Status", "Pending");

                    // Save and reprocess application
                    jobHistoryData.add("JobHistory", jobHistory);
                    saveData(jobHistoryData, JOB_HISTORY_DATA_FILE);

                    // Reload job data for slot update
                    JsonObject jobData = loadData(JOB_DATA_FILE);
                    if (jobData != null && jobData.has("Jobs")) {
                        JsonArray jobs = jobData.getAsJsonArray("Jobs");
                        for (JsonElement jobElem : jobs) {
                            JsonObject jobInfo = jobElem.getAsJsonObject();
                            if (jobInfo.get("Title").getAsString().equals(jobTitle)) {
                                return processJobApplication(username, jobTitle, jobInfo, jobData);
                            }
                        }
                    }

                    return "Job not found while reapplying.";
                }

                // Already applied and still pending
                if (status.equals("Pending")) {
                    System.out.println(username + " has already applied for: " + jobTitle);
                    logServerMessage(username, "ApplyForJob", "Already applied for " + jobTitle);
                    return "You have already applied for this job.";
                }
            }
        }

        // Apply as a new applicant
        JsonObject jobData = loadData(JOB_DATA_FILE);
        if (jobData != null && jobData.has("Jobs")) {
            JsonArray jobs = jobData.getAsJsonArray("Jobs");
            for (JsonElement jobElem : jobs) {
                JsonObject job = jobElem.getAsJsonObject();
                if (job.get("Title").getAsString().equals(jobTitle)) {
                    return processJobApplication(username, jobTitle, job, jobData);
                }
            }
        }

        logServerMessage(username, "ApplyForJob", "Job not found: " + jobTitle);
        System.out.println("Job not found: " + jobTitle);
        return "Job not found.";
    }

    private String processJobApplication(String username, String jobTitle, JsonObject job, JsonObject jobData) {
        int availableSlots = job.get("MaxApplicants").getAsInt();

        if (availableSlots > 0) {
            // Decrement available slots
            job.addProperty("MaxApplicants", availableSlots - 1);
            saveData(jobData, JOB_DATA_FILE);

            // Create new application
            JsonObject newApplication = new JsonObject();
            newApplication.addProperty("username", username);
            newApplication.addProperty("Title", job.get("Title").getAsString());
            newApplication.addProperty("CompanyName", job.get("CompanyName").getAsString());
            newApplication.addProperty("JobLocation", job.get("JobLocation").getAsString());
            newApplication.addProperty("Salary", job.get("Salary").getAsInt());
            newApplication.addProperty("Status", "Pending");

            // Load or init job history
            JsonObject jobHistoryData = loadData(JOB_HISTORY_DATA_FILE);
            if (jobHistoryData == null || !jobHistoryData.has("JobHistory")) {
                jobHistoryData = new JsonObject();
                jobHistoryData.add("JobHistory", new JsonArray());
            }

            JsonArray jobHistory = jobHistoryData.getAsJsonArray("JobHistory");
            jobHistory.add(newApplication);
            jobHistoryData.add("JobHistory", jobHistory);
            saveData(jobHistoryData, JOB_HISTORY_DATA_FILE);

            logServerMessage(username, "ApplyForJob", "Successfully applied for " + jobTitle);
            System.out.println(username + " successfully applied for " + jobTitle);
            return "Application successful. See History for checking application status.";
        } else {
            logServerMessage(username, "ApplyForJob", "No slots available for " + jobTitle);
            System.out.println("No slots available for " + jobTitle);
            return "No slots available.";
        }
    }

    @Override
    public String getCredentials(String username) throws RemoteException {
        JsonObject userData = (JsonObject) loadData(USER_DATA_FILE);
        if (userData == null || !userData.has("users")) {
            return "User data not found.";
        }

        JsonArray users = userData.getAsJsonArray("users");
        for (JsonElement userElement : users) {
            JsonObject user = userElement.getAsJsonObject();
            if (user.get("username").getAsString().equals(username)) {
                return "Username: " + username + ", Role: " + user.get("role").getAsString();
            }
        }
        return "User not found.";
    }

    @Override
    public String updateCredentials(String oldUsername, String newUsername, String newPassword) throws RemoteException {
        System.out.println("User " + oldUsername + " is updating credentials...");
        JsonObject userData = loadData(USER_DATA_FILE);
        if (userData == null || !userData.has("users")) {
            return "User data not found.";
        }

        JsonArray users = userData.getAsJsonArray("users");
        boolean updated = false;

        for (JsonElement userElement : users) {
            JsonObject user = userElement.getAsJsonObject();
            if (user.get("username").getAsString().equals(oldUsername)) {
                user.addProperty("username", newUsername);
                user.addProperty("password", newPassword);
                updated = true;
                break;
            }
        }

        if (!updated) {
            System.out.println("User not found: " + oldUsername);
            return "User not found.";
        }

        saveData(userData, USER_DATA_FILE);
        System.out.println("Credentials updated for: " + newUsername);
        return "User credentials updated successfully.";
    }

    @Override
    public String deleteAccount(String username) throws RemoteException {
        System.out.println("User " + username + " is requesting account deletion...");
        JsonObject userData = (JsonObject) loadData(USER_DATA_FILE);
        if (userData == null || !userData.has("users")) {
            return "User data not found.";
        }

        JsonArray users = userData.getAsJsonArray("users");
        JsonArray updatedUsers = new JsonArray();
        boolean deleted = false;

        for (JsonElement userElement : users) {
            JsonObject user = userElement.getAsJsonObject();
            if (!user.get("username").getAsString().equals(username)) {
                updatedUsers.add(user);
            } else {
                deleted = true;
            }
        }

        if (!deleted) {
            System.out.println("User not found: " + username);
            return "User not found.";
        }

        userData.add("users", updatedUsers);
        saveData(userData, USER_DATA_FILE);
        System.out.println("Account deleted for: " + username);
        return "Account deleted successfully.";
    }


    @Override
    public String searchJobs(String query) throws RemoteException {
        JsonObject data = (JsonObject) loadData(JOB_DATA_FILE);
        StringBuilder jobs = new StringBuilder();
        boolean isJobFound = false;

        if (data != null && data.has("Jobs")) {
            JsonArray jobList = data.getAsJsonArray("Jobs");

            for (JsonElement jobElement : jobList) {
                JsonObject job = jobElement.getAsJsonObject();
                String title = job.get("Title").getAsString().toLowerCase();
                String companyName = job.get("CompanyName").getAsString().toLowerCase();
                String location = job.get("JobLocation").getAsString().toLowerCase();

                if (title.contains(query.toLowerCase()) ||
                        companyName.contains(query.toLowerCase()) ||
                        location.contains(query.toLowerCase())) {

                    isJobFound = true;

                    jobs.append(job.get("Title").getAsString()).append("|")
                            .append(job.get("CompanyName").getAsString()).append("|")
                            .append(job.get("JobLocation").getAsString()).append("|")
                            .append(job.get("MaxApplicants").getAsInt()).append("|")
                            .append(job.get("Salary").getAsInt()).append("|")
                            .append(job.get("Description").getAsString()).append("|")
                            .append(job.get("Status").getAsString()).append("\n");
                }
            }
        }

        return isJobFound ? jobs.toString().trim() : "No jobs found";
    }

    @Override
    public String getJobHistory(String username) throws RemoteException {
        JsonElement data = loadData(JOB_HISTORY_DATA_FILE);

        // Check if data is null or not a JSON array
        if (data == null || (!data.isJsonObject() && !data.isJsonArray())) {
            return "[]";
        }

        JsonArray jobs;
        if (data.isJsonObject()) {
            // If the data is a JSON object containing an array
            jobs = data.getAsJsonObject().getAsJsonArray("JobHistory");
        } else {
            // If the data itself is a JSON array
            jobs = data.getAsJsonArray();
        }

        JsonArray result = new JsonArray();

        for (JsonElement jobElement : jobs) {
            if (jobElement.isJsonObject()) {
                JsonObject job = jobElement.getAsJsonObject();
                if (job.has("username") && job.get("username").getAsString().equals(username)) {
                    result.add(job);
                }
            }
        }

        return result.toString();
    }

    @Override
    public String cancelJobApplication(String username, String jobTitle) throws RemoteException {
        JsonObject jobHistoryData = (JsonObject) loadData(JOB_HISTORY_DATA_FILE);
        if (jobHistoryData == null || !jobHistoryData.has("JobHistory")) {
            return "No job applications found.";
        }

        JsonArray jobHistory = jobHistoryData.getAsJsonArray("JobHistory");
        JsonObject jobData = (JsonObject) loadData(JOB_DATA_FILE);

        if (jobData == null || !jobData.has("Jobs")) {
            return "Job data not found.";
        }

        JsonArray jobs = jobData.getAsJsonArray("Jobs");
        JsonArray updatedJobHistory = new JsonArray();
        boolean applicationFound = false;

        for (JsonElement jobElement : jobHistory) {
            JsonObject job = jobElement.getAsJsonObject();
            if (job.get("Title").getAsString().equals(jobTitle) && job.get("username").getAsString().equals(username)) {
                applicationFound = true;
            } else {
                updatedJobHistory.add(job);
            }
        }

        if (!applicationFound) {
            return "You have not applied for this job.";
        }

        for (JsonElement jobElement : jobs) {
            JsonObject job = jobElement.getAsJsonObject();
            if (job.get("Title").getAsString().equals(jobTitle)) {
                int availableSlots = job.get("MaxApplicants").getAsInt();
                job.addProperty("MaxApplicants", availableSlots + 1);
                break;
            }
        }


        jobHistoryData.add("JobHistory", updatedJobHistory);
        saveUpdatedJobHistory(jobHistoryData, JOB_HISTORY_DATA_FILE);
        saveDataJobPost(jobData, JOB_DATA_FILE);

        return "Job application canceled successfully.";
    }

    private void saveUpdatedJobHistory(JsonObject updatedData, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(updatedData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonObject loadData(String filePath) {
        File file = new File(filePath);

        // Check if the file exists
        if (!file.exists()) {
            return null; // Return null to indicate failure to load
        }

        try (FileReader reader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(reader)) {

            // Read the entire file at once
            StringBuilder rawContent = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                rawContent.append(line);
            }

            // Parse the raw content as JSON
            JsonElement jsonElement = JsonParser.parseString(rawContent.toString());

            // Check if the parsed content is a JSON object
            if (jsonElement.isJsonObject()) {
                return jsonElement.getAsJsonObject();
            } else if (jsonElement.isJsonArray()) {
                JsonObject wrapper = new JsonObject();
                wrapper.add("users", jsonElement.getAsJsonArray());
                return wrapper;
            }
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null; // Return null if any error occurs
    }

    private void saveDataJobHistory(JsonObject newData, String filePath) {
        JsonObject existingData = (JsonObject) loadData(filePath);
        if (existingData == null) {
            existingData = new JsonObject();
            existingData.add("JobHistory", new JsonArray());
        }
        JsonArray existingJobs = existingData.getAsJsonArray("JobHistory");
        JsonArray newJobs = newData.has("JobHistory") ? newData.getAsJsonArray("JobHistory") : new JsonArray();

        if (newJobs != null) {
            for (JsonElement job : newJobs) {
                JsonObject newJob = job.getAsJsonObject();
                String newUsername = newJob.get("username").getAsString();
                String newJobTitle = newJob.get("Title").getAsString();
                boolean exists = false;
                for (JsonElement existingJob : existingJobs) {
                    JsonObject existingJobObj = existingJob.getAsJsonObject();
                    if (existingJobObj.get("username").getAsString().equals(newUsername) &&
                            existingJobObj.get("Title").getAsString().equals(newJobTitle)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    existingJobs.add(newJob);
                }
            }
        }

        existingData.add("JobHistory", existingJobs);
        try (FileWriter writer = new FileWriter(filePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(existingData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDataJobPost(JsonObject updatedPosts, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Convert the updatedPosts array to a JSON string and write it to the file
            writer.write(updatedPosts.toString());  // Indent with 4 spaces for readability
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveData(JsonObject data, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(data.toString());
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    @Override
    public String addJob(String employer, String title, String description, String location, String companyName, int maxApplicants, int salary) throws RemoteException {
        System.out.println("Employer " + employer + " is adding a new job: " + title + " at " + companyName);
        JsonObject jobData = (JsonObject) loadData(JOB_DATA_FILE);

        if (jobData == null) {
            jobData = new JsonObject();
            jobData.add("Job", new JsonArray());
        }

        JsonArray jobs = jobData.getAsJsonArray("Job");
        JsonObject newJob = new JsonObject();
        newJob.addProperty("Employer", employer);
        newJob.addProperty("Title", title);
        newJob.addProperty("CompanyName", companyName);
        newJob.addProperty("JobLocation", location);
        newJob.addProperty("MaxApplicants", maxApplicants);
        newJob.addProperty("Salary", salary);
        newJob.addProperty("Description", description);
        newJob.addProperty("Status", "Active");

        jobs.add(newJob);
        jobData.add("Jobs", jobs);
        saveDataJobPost(jobData, JOB_DATA_FILE);

        System.out.println("Job added successfully: " + title + " at " + companyName);
        return "Job added successfully.";
    }

    @Override
    public String deleteJob(String employer, String title, String companyName) throws RemoteException {
        System.out.println("Employer " + employer + " is attempting to delete the job: " + title + " at " + companyName);
        JsonObject jobData = (JsonObject) loadData(JOB_DATA_FILE);

        if (jobData == null || !jobData.has("Jobs")) {
            System.out.println("No job postings found.");
            return "No job postings found.";
        }

        JsonArray jobs = jobData.getAsJsonArray("Jobs");
        JsonArray updatedJobs = new JsonArray();
        boolean jobDeleted = false;

        for (JsonElement jobElement : jobs) {
            JsonObject job = jobElement.getAsJsonObject();

            if (job.get("Employer").getAsString().equals(employer) &&
                    job.get("Title").getAsString().equals(title) &&
                    job.get("CompanyName").getAsString().equals(companyName)) {
                jobDeleted = true;
                System.out.println("Job deleted: " + title + " at " + companyName);
            } else {
                updatedJobs.add(job);
            }
        }

        if (!jobDeleted) {
            System.out.println("Job not found or no permission to delete: " + title + " at " + companyName);
            return "Job not found or you don't have permission to delete it.";
        }

        jobData.add("Jobs", updatedJobs);
        saveDataJobPost(jobData, JOB_DATA_FILE);

        return "Job deleted successfully.";
    }

    @Override
    public String getActiveJobs(String currentUser) throws RemoteException {
        JsonObject data = (JsonObject) loadData(JOB_DATA_FILE);
        if (data != null && data.has("Jobs")) {
            JsonArray jobs = data.getAsJsonArray("Jobs");
            JsonArray activeJobs = new JsonArray();

            for (JsonElement jobElement : jobs) {
                JsonObject job = jobElement.getAsJsonObject();
                if (job.get("Status").getAsString().equalsIgnoreCase("Active") &&
                        job.get("Employer").getAsString().equalsIgnoreCase(currentUser)) {
                    activeJobs.add(job);
                }
            }
            return activeJobs.toString();
        }
        return "No active jobs available.";
    }

    @Override
    public String getUserJobs(String currentUser) throws RemoteException {
        JsonObject data = (JsonObject) loadData(JOB_DATA_FILE);
        if (data != null && data.has("Jobs")) {
            JsonArray jobs = data.getAsJsonArray("Jobs");
            JsonArray userJobs = new JsonArray();

            for (JsonElement jobElement : jobs) {
                JsonObject job = jobElement.getAsJsonObject();
                if (job.get("Employer").getAsString().equalsIgnoreCase(currentUser)) {
                    userJobs.add(job);
                }
            }
            return userJobs.toString();
        }
        return "No jobs found.";
    }

    @Override
    public String getClosedJobs(String currentUser) throws RemoteException {
        JsonObject data = (JsonObject) loadData(JOB_DATA_FILE);
        if (data != null && data.has("Jobs")) {
            JsonArray jobs = data.getAsJsonArray("Jobs");
            JsonArray closedJobs = new JsonArray();

            for (JsonElement jobElement : jobs) {
                JsonObject job = jobElement.getAsJsonObject();
                if (!job.get("Status").getAsString().equalsIgnoreCase("Active") &&
                        job.get("Employer").getAsString().equalsIgnoreCase(currentUser)) {
                    closedJobs.add(job);
                }
            }
            return closedJobs.toString();
        }
        return "No closed jobs available.";
    }

    @Override
    public String signup(String username, String password, String role) throws RemoteException {
        System.out.println("Attempting to sign up user: " + username + " with role: " + role);
        JsonObject userData = (JsonObject) loadData(USER_DATA_FILE);

        if (userData == null) {
            userData = new JsonObject();
            userData.add("users", new JsonArray());
        }

        JsonArray users = userData.getAsJsonArray("users");
        for (JsonElement user : users) {
            JsonObject userObj = user.getAsJsonObject();
            if (userObj.get("username").getAsString().equalsIgnoreCase(username)) {
                System.out.println("Signup failed: Username " + username + " already exists!");
                return "Username already exists!";
            }
        }

        JsonObject newUser = new JsonObject();
        newUser.addProperty("username", username);
        newUser.addProperty("password", password);
        newUser.addProperty("role", role.toLowerCase());
        users.add(newUser);
        userData.add("users", users);
        saveData(userData, USER_DATA_FILE);

        System.out.println("Signup successful for user: " + username);
        return "Signup successful";
    }

    @Override
    public String searchJobEmployer(String currentUser, String query) throws RemoteException {
        JsonObject data = (JsonObject) loadData(JOB_DATA_FILE);
        JsonArray resultArray = new JsonArray();
        boolean isJobFound = false;

        if (data != null && data.has("Jobs")) {
            JsonArray jobList = data.getAsJsonArray("Jobs");

            for (JsonElement jobElement : jobList) {
                JsonObject job = jobElement.getAsJsonObject();
                String employer = job.get("Employer").getAsString();
                if (!employer.equalsIgnoreCase(currentUser)) {
                    continue;
                }

                String title = job.get("Title").getAsString().toLowerCase();
                String companyName = job.get("CompanyName").getAsString().toLowerCase();
                String location = job.get("JobLocation").getAsString().toLowerCase();

                if (title.contains(query.toLowerCase()) ||
                        companyName.contains(query.toLowerCase()) ||
                        location.contains(query.toLowerCase())) {

                    isJobFound = true;
                    JsonObject jobCopy = job.deepCopy();
                    resultArray.add(jobCopy);
                }
            }
        }

        return isJobFound ? resultArray.toString() : "No jobs found";
    }

    @Override
    public String getJobApplicants(String username, String jobTitle, String companyName) throws RemoteException {
        try {
            System.out.println("Fetching applicants for job: " + jobTitle + " at " + companyName + " by user: " + username);

            // Load the JSON data from the file
            FileReader reader = new FileReader("res/JobHistory.json");
            JsonElement jsonElement = JsonParser.parseReader(reader);
            reader.close();

            if (!jsonElement.isJsonObject()) {
                System.err.println("Error: Root element is not a JSON object.");
                return "{\"JobHistory\": []}";
            }

            JsonObject rootObject = jsonElement.getAsJsonObject();
            if (!rootObject.has("JobHistory") || !rootObject.get("JobHistory").isJsonArray()) {
                System.err.println("Error: JobHistory field is missing or not an array.");
                return "{\"JobHistory\": []}";
            }

            JsonArray applicantsArray = rootObject.getAsJsonArray("JobHistory");
            JsonArray resultArray = new JsonArray();

            // Filter applicants based on job title and company name
            for (JsonElement element : applicantsArray) {
                JsonObject applicant = element.getAsJsonObject();
                if (applicant.get("Title").getAsString().equals(jobTitle) &&
                        applicant.get("CompanyName").getAsString().equals(companyName)) {
                    resultArray.add(applicant);
                }
            }

            JsonObject response = new JsonObject();
            response.add("JobHistory", resultArray);
            System.out.println("Retrieved " + resultArray.size() + " applicants for the job: " + jobTitle);

            return response.toString();

        } catch (Exception e) {
            System.err.println("Error retrieving applicants for job: " + jobTitle);
            e.printStackTrace();
            JsonObject errorObject = new JsonObject();
            errorObject.addProperty("error", "Error retrieving applicants");
            return errorObject.toString();
        }
    }

    @Override
    public String acceptApplicant(String username, String jobTitle, String companyName, String applicantEmail) throws RemoteException {
        return updateApplicantStatus(username, jobTitle, companyName, applicantEmail);
    }

    @Override
    public String rejectApplicant(String username, String jobTitle, String companyName, String applicantEmail) throws RemoteException {
        return updateApplicantStatus(username, jobTitle, companyName, applicantEmail);
    }

    public String updateApplicantStatus(String username, String Title, String CompanyName, String applicantEmail) {
        try {
            String newStatus = "";
            System.out.println("Updating status of applicant: " + applicantEmail + " for job: " + Title + " at " + CompanyName + " to: " + newStatus);

            // Load the JSON data from the file
            FileReader reader = new FileReader("res/JobHistory.json");
            JsonElement jsonElement = JsonParser.parseReader(reader);
            reader.close();

            if (!jsonElement.isJsonObject()) {
                System.err.println("Error: Root element is not a JSON object.");
                return "Error updating applicant status";
            }

            JsonObject rootObject = jsonElement.getAsJsonObject();
            if (!rootObject.has("JobHistory") || !rootObject.get("JobHistory").isJsonArray()) {
                System.err.println("Error: JobHistory field is missing or not an array.");
                return "Error updating applicant status";
            }

            JsonArray applicantsArray = rootObject.getAsJsonArray("JobHistory");
            boolean updated = false;

            for (JsonElement element : applicantsArray) {
                JsonObject applicant = element.getAsJsonObject();
                if (applicant.get("username").getAsString().equals(username) &&
                        applicant.get("Title").getAsString().equals(Title) &&
                        applicant.get("CompanyName").getAsString().equals(CompanyName)) {
                    applicant.addProperty("Status", newStatus);
                    updated = true;
                    break;
                }
            }

            if (updated) {
                try (FileWriter writer = new FileWriter("res/JobHistory.json")) {
                    new Gson().toJson(rootObject, writer);
                }
                System.out.println("Applicant " + applicantEmail + " status updated to: " + newStatus);
                return "Applicant " + newStatus.toLowerCase() + " successfully!";
            }

            System.out.println("Applicant not found: " + applicantEmail);
            return "Applicant not found.";

        } catch (Exception e) {
            System.err.println("Error updating applicant status for " + applicantEmail);
            e.printStackTrace();
            return "Error updating applicant status";
        }
    }

    private void logServerMessage(String username, String action, String message) {
        File file = new File(SERVER_MESSAGE_LOG_FILE);
        JsonArray messageLog = new JsonArray();

        // Load existing log if the file exists
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                JsonElement element = JsonParser.parseReader(reader);
                if (element.isJsonArray()) {
                    messageLog = element.getAsJsonArray();
                }
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
            }
        }

        // Create log entry
        JsonObject logEntry = new JsonObject();

        // Format the timestamp to a human-readable format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                .withZone(ZoneId.systemDefault());
        String timestamp = formatter.format(Instant.now());

        // Ensure that timestamp is added as a string
        logEntry.addProperty("timestamp", timestamp); // It should now correctly add as a string

        logEntry.addProperty("username", username);
        logEntry.addProperty("action", action);
        logEntry.addProperty("message", message);

        // Add to log
        messageLog.add(logEntry);

        // Save back to the file
        try (FileWriter writer = new FileWriter(file)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(messageLog, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}