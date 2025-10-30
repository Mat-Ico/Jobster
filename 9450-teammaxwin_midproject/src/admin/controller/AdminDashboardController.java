package admin.controller;

import admin.view.AdminDashboardView;
import admin.view.AdminPostView;
import admin.view.AdminUserView;

import javax.swing.*;

public class AdminDashboardController {
    private final AdminDashboardView dashboardView;

    public AdminDashboardController(AdminDashboardView view) {
        this.dashboardView = view;

        view.addUserButtonListener(s -> openUserManagement());
        view.addPostButtonListener(s -> openPostManagement());
        view.addTurnOffButtonListener(s -> turnOffServer());
    }

    private void openUserManagement() {
        dashboardView.close();
        SwingUtilities.invokeLater(() -> {
            AdminUserView userView = new AdminUserView();
            new AdminUserController(userView);
        });
    }

    private void openPostManagement() {
        if (!AdminPostController.isPostViewOpen) {
            dashboardView.close();
            SwingUtilities.invokeLater(() -> {
                AdminPostView postView = new AdminPostView();
                new AdminPostController( postView, this);
            });
        }
    }

    private void turnOffServer() {
        int confirm = JOptionPane.showConfirmDialog(
                dashboardView.getFrame(),
                "Are you sure you want to turn off the server?",
                "Confirm Shutdown",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public void showAdminDashboard() {
        SwingUtilities.invokeLater(() -> dashboardView.setVisible(true));
    }

}
