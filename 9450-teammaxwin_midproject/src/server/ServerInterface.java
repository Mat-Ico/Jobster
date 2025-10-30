package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    boolean login(String username, String password) throws RemoteException;
    String getJobs() throws RemoteException;
    String updateJobStatus(String Title, String status) throws RemoteException;
    String applyForJob(String username, String Title) throws RemoteException;
    String getCredentials(String username) throws RemoteException;
    String updateCredentials(String username, String password, String newPassword) throws RemoteException;
    String deleteAccount(String username) throws RemoteException;
    String searchJobs(String query) throws RemoteException;
    String getJobHistory(String username) throws RemoteException;
    String cancelJobApplication(String username, String Title) throws RemoteException;
    String authenticate(String username, String password) throws RemoteException;
    String addJob(String employer, String Title, String description, String location, String companyName, int maxApplicants, int salary) throws RemoteException;
    String deleteJob(String employer, String Title, String companyName) throws RemoteException;
    String getClosedJobs(String currentUser) throws RemoteException;
    String getActiveJobs(String currentUser) throws RemoteException;
    String signup(String username, String password, String role) throws RemoteException;
    String getUserJobs(String currentUser) throws RemoteException;
    String searchJobEmployer(String currentUser, String query) throws RemoteException;
    String getJobApplicants(String username, String Title, String companyName) throws RemoteException;
    String acceptApplicant(String username, String Title, String companyName, String applicantEmail) throws RemoteException;
    String rejectApplicant(String username, String Title, String companyName, String applicantEmail) throws RemoteException;
    String updateApplicantStatus(String username, String Title, String CompanyName, String applicantEmail) throws RemoteException;
}
