package server;

import admin.controller.AdminDashboardController;
import admin.view.AdminDashboardView;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerTest {
    public static void main(String[] args) {
        try {
            ServerImpl server = new ServerImpl();
            Registry reg= LocateRegistry.createRegistry(2000);
            reg.rebind("RMI", server);

            System.out.println("Server is running...");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        AdminDashboardView dashboardView = new AdminDashboardView();
        new AdminDashboardController(dashboardView);
        dashboardView.getFrame().setVisible(true);
    }
}

