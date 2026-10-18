package Server.WebPage;

import Server.Controller.AuthenticationController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping({"/", "/login"})
    public String login(@CookieValue(value = "SESSION", required = false) String session) { 
        if (AuthenticationController.isValidSession(session)) {
            return "redirect:/dashboard";
        }
        return "loginView"; }

    @GetMapping({"/dashboard"})
    public String dashboard(@CookieValue(value = "SESSION", required = false) String session) { 
        if (!AuthenticationController.isValidSession(session)) {
            return "redirect:/login";
        }
        return "dashboardView"; }

    @GetMapping({"/heatmap"})
    public String heatmap(@CookieValue(value = "SESSION", required = false) String session) { 
        if (!AuthenticationController.isValidSession(session)) {
            return "redirect:/login";
        }
        return "heatmapView"; }

    @GetMapping({"/reports"})
    public String reports(@CookieValue(value = "SESSION", required = false) String session) { 
        if (!AuthenticationController.isValidSession(session)) {
            return "redirect:/login";
        }
        return "reportsView"; }

    @GetMapping({"/settings"})
    public String settings(@CookieValue(value = "SESSION", required = false) String session) { 
        if (!AuthenticationController.isValidSession(session)) {
            return "redirect:/login";
        }
        return "settingsView"; }
}
