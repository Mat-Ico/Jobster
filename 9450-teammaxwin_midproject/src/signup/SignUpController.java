package signup;


import server.ServerInterface;
import login.LogInController;

import java.rmi.RemoteException;

public class SignUpController {
    private SignUpModel model;
    private SignUpView view;
    private LogInController loginController;
    private ServerInterface server;

    public SignUpController(LogInController loginController) {
        this.loginController = loginController;
        model = new SignUpModel();
    }

    public void openSignUpView( ServerInterface server) {
        this.server = server;
        if (view == null) {
            view = new SignUpView(this);
        } else {
            view.getFrame().setVisible(true);
        }
    }

    public void registerUser(String username, String password, String role) throws RemoteException {
        String serverRole = role.equals("Job Finder") ? "jobfinder" : "employer";

        if (username.isEmpty() || password.isEmpty()) {
            view.displayErrorMessage("Username and password cannot be empty.");
            return;
        }
        String response = model.sendSignupRequest(username, password, serverRole,server);
        if ("Signup successful".equals(response)) {
            view.displayMessage("User successfully registered!");
            view.close();
            loginController.openLoginView();
        } else {
            view.displayErrorMessage(response);
        }
    }

    public void openLoginView() {
        if (view != null) {
            view.close();
        }
        loginController.openLoginView();
    }
}

