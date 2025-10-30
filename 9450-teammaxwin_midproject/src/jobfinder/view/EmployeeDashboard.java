package jobfinder.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class EmployeeDashboard {
    private JFrame frame;
    private JTextField searchField;
    private JButton dashboardButton, jobHistoryButton, editAccountButton, logOutButton;
    private JTabbedPane jobTabs;
    private JPanel centerCards;
    private CardLayout cardLayout;
    private JTextField editUsernameField;
    private JPasswordField editPasswordField;
    private JLabel editSuccessMessage, editErrorMessage;
    private JButton editSaveButton, editDeleteButton;

    public EmployeeDashboard() {
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        frame = new JFrame("Jobster");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 400);
        frame.setLayout(new BorderLayout());

        // Sidebar Panel
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(173, 216, 230));
        sidebar.setLayout(null);
        sidebar.setPreferredSize(new Dimension(190, frame.getHeight()));

        ImageIcon originalIcon = new ImageIcon("img/jobster.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(130, 140, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(scaledImage));
        logo.setBounds(30, 10, 130, 140);

        searchField = new JTextField("Search for Jobs");
        searchField.setBounds(10, 160, 165, 26);

        dashboardButton = new JButton("Dashboard");
        dashboardButton.setBounds(40, 195, 110, 30);

        jobHistoryButton = new JButton("Job History");
        jobHistoryButton.setBounds(40, 230, 110, 30);

        editAccountButton = new JButton("Edit Account");
        editAccountButton.setBounds(40, 265, 110, 30);

        logOutButton = new JButton("Log Out");
        logOutButton.setBounds(40, 300, 110, 30);

        sidebar.add(logo);
        sidebar.add(searchField);
        sidebar.add(dashboardButton);
        sidebar.add(jobHistoryButton);
        sidebar.add(editAccountButton);
        sidebar.add(logOutButton);

        // Main Panel with CardLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Find Your Next Job Here", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        centerCards = new JPanel(cardLayout);

        jobTabs = new JTabbedPane();
        centerCards.add(jobTabs, "jobs");

        JPanel editAccountPanel = createEditAccountPanel();
        centerCards.add(editAccountPanel, "admin.view.editAccount");

        JPanel dashboardPanel = new JPanel();
        dashboardPanel.add(new JLabel("Welcome to Dashboard!"));
        centerCards.add(dashboardPanel, "dashboard");


        JPanel jobHistoryPanel = new JPanel();
        jobHistoryPanel.add(new JLabel("Your Job History"));
        centerCards.add(jobHistoryPanel, "jobHistory");



        mainPanel.add(centerCards, BorderLayout.CENTER);

        frame.add(sidebar, BorderLayout.WEST);
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private JPanel createEditAccountPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel titleLabel = new JLabel("Edit Your Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBounds(190, 20, 300, 30);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(80, 80, 100, 20);

        editUsernameField = new JTextField();
        editUsernameField.setBounds(150, 80, 200, 25);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(80, 120, 100, 20);

        editPasswordField = new JPasswordField();
        editPasswordField.setBounds(150, 120, 200, 25);

        editSuccessMessage = new JLabel("Account updated successfully!");
        editSuccessMessage.setForeground(Color.GREEN);
        editSuccessMessage.setBounds(250, 160, 250, 20);
        editSuccessMessage.setVisible(false);

        editErrorMessage = new JLabel("Fields cannot be empty!");
        editErrorMessage.setForeground(Color.RED);
        editErrorMessage.setBounds(260, 180, 250, 20);
        editErrorMessage.setVisible(false);

        editSaveButton = new JButton("Save Changes");
        editSaveButton.setBounds(80, 220, 140, 30);

        editDeleteButton = new JButton("Delete Account");
        editDeleteButton.setBounds(240, 220, 140, 30);

        panel.add(titleLabel);
        panel.add(usernameLabel);
        panel.add(editUsernameField);
        panel.add(passwordLabel);
        panel.add(editPasswordField);
        panel.add(editSuccessMessage);
        panel.add(editErrorMessage);
        panel.add(editSaveButton);
        panel.add(editDeleteButton);

        return panel;
    }

    public JTextField getEditUsernameField() {
        return editUsernameField;
    }

    public JPasswordField getEditPasswordField() {
        return editPasswordField;
    }

    public JButton getEditSaveButton() {
        return editSaveButton;
    }

    public JButton getEditDeleteButton() {
        return editDeleteButton;
    }

    public JLabel getEditSuccessMessage() {
        return editSuccessMessage;
    }

    public JLabel getEditErrorMessage() {
        return editErrorMessage;
    }

    public void showEditAccountCard() {
        cardLayout.show(centerCards, "admin.view.editAccount");
    }

    public void showJobsCard() {
        cardLayout.show(centerCards, "jobs");
    }

    // Existing getters for other components
    public JFrame getFrame() {
        return frame;
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public JButton getDashboardButton() {
        return dashboardButton;
    }

    public JButton getJobHistoryButton() {
        return jobHistoryButton;
    }

    public JButton getEditAccountButton() {
        return editAccountButton;
    }

    public JButton getLogOutButton() {
        return logOutButton;
    }

    public JTabbedPane getJobTabs() {
        return jobTabs;
    }

    public void setJobTabs(String[][] jobs, ActionListener applyListener) {
        jobTabs.removeAll();

        for (String[] job : jobs) {
            if (job.length < 7) continue;

            String title = job[0];
            String company = job[1];
            String location = job[2];
            String maxApplicants = job[3];
            String salary = job[4];
            String description = job[5];
            String status = job[6];

            JPanel jobPanel = new JPanel(new BorderLayout());

            JTextArea jobDetails = new JTextArea(
                    "Company: " + company + "\n" +
                            "Location: " + location + "\n" +
                            "Max Applicants: " + maxApplicants + "\n" +
                            "Salary: $" + salary + "\n" +
                            "Description: " + description + "\n" +
                            "Status: " + status
            );
            jobDetails.setEditable(false);
            jobPanel.add(new JScrollPane(jobDetails), BorderLayout.CENTER);

            JButton applyButton = new JButton("Apply");
            applyButton.setEnabled(status.equalsIgnoreCase("Active"));
            applyButton.setActionCommand(title);
            applyButton.addActionListener(applyListener);

            jobPanel.add(applyButton, BorderLayout.SOUTH);
            jobTabs.addTab(title, jobPanel);
        }
    }

    public void setJobHistoryTabs(String[][] jobHistory, ActionListener cancelListener) {
        jobTabs.removeAll();

        for (String[] job : jobHistory) {
            if (job.length < 5) continue;

            String title = job[0];
            String company = job[1];
            String location = job[2];
            String salary = job[3];
            String status = job[4];

            JPanel jobPanel = new JPanel(new BorderLayout());

            JTextArea jobDetails = new JTextArea(
                    "Company: " + company + "\n" +
                            "Location: " + location + "\n" +
                            "Salary: $" + salary + "\n" +
                            "Status: " + status
            );
            jobDetails.setEditable(false);
            jobPanel.add(new JScrollPane(jobDetails), BorderLayout.CENTER);

            if (status.equalsIgnoreCase("Pending")) {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.setActionCommand(title);
                cancelButton.addActionListener(cancelListener);
                jobPanel.add(cancelButton, BorderLayout.SOUTH);
            }

            jobTabs.addTab(title, jobPanel);
        }
    }
    public void showDashboardCard() {
        cardLayout.show(centerCards, "dashboard");
    }

    public void showJobHistoryCard() {
        cardLayout.show(centerCards, "jobHistory");
    }
    private void initializeListeners() {
        dashboardButton.addActionListener(e -> showDashboardCard());
        jobHistoryButton.addActionListener(e -> showJobHistoryCard());
        editAccountButton.addActionListener(e -> showEditAccountCard());
    }
}

