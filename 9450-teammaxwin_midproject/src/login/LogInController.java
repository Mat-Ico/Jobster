package login;

import server.ServerImpl;
import server.ServerInterface;

import employer.EmployerDashboard;
import employer.EmployerDashboardController;
import jobfinder.controller.EmployeeDashboardController;
import jobfinder.view.EmployeeDashboard;
import signup.SignUpController;


import javax.swing.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class LogInController {
    public final LogInView view;
    private final SignUpController signUpController;
    private String currentUsername;
    private ServerInterface server;

    public LogInController(ServerInterface server) {
        this.view = new LogInView(this);
        this.signUpController = new SignUpController(this);
        this.server = server;

    }

    public void authenticateUser(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            view.getStatusLabel().setText("Fields cannot be empty.");
            return;
        }
        try {

            String response = server.authenticate(username, password);

            SwingUtilities.invokeLater(() -> {
                if ("jobfinder".equals(response)) {
                    JOptionPane.showMessageDialog(view.getFrame(), "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    view.getStatusLabel().setText("Login successful!");
                    view.close();
                    EmployeeDashboard view = new EmployeeDashboard();
                    new EmployeeDashboardController(view,server, username);

                } else if ("employer".equals(response)) {
                    JOptionPane.showMessageDialog(view.getFrame(), "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    view.getStatusLabel().setText("Login successful!");
                    view.close();
                    currentUsername = username;
                    EmployerDashboard view = new EmployerDashboard();
                    new EmployerDashboardController(view,server, username);

                } else {
                    view.displayErrorMessage(response);
                    view.getStatusLabel().setText(response);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> view.getStatusLabel().setText("Error connecting to server."));
        }
    }

    public void openSignUpView() {
        view.close();
        signUpController.openSignUpView(server);
    }

    public void openLoginView() {
        if (view != null) {
            view.getFrame().setVisible(true);
        }
    }

    public static void main(String[] args) {
        try {
            ServerInterface server = (ServerInterface) Naming.lookup("//localhost:2000/RMI");
            LogInController loginController = new LogInController(server);
            loginController.view.getFrame().setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

