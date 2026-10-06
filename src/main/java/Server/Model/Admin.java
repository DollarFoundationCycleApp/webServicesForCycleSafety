package Server.Model;

import java.util.Collections;
import java.util.List;


public class Admin extends User {
    
    private String adminRole;

    public Admin(String userId, String username, String password) {
        super(userId, username, password);
    }


    public boolean manageIncidents(String incidentId, String action) {




        return false;
    }

    public List<Report> viewReports() {

        return Collections.emptyList();
    }

}