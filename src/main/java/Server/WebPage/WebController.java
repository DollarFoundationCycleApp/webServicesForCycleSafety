package Server.WebPage;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping({"/", "/login"})
    public String login() { return "loginView"; }

    @GetMapping({"/dashboard"})
    public String dashboard() { return "dashboardView"; }

    @GetMapping({"/heatmap"})
    public String heatmap() { return "heatmapView"; }

    @GetMapping({"/reports"})
    public String reports() { return "reportsView"; }

    @GetMapping({"/settings"})
    public String settings() { return "settingsView"; }
}
