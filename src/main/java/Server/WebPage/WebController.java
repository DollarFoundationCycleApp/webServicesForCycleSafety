package Server.WebPage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @Value("${map.style-url:}")
    private String mapStyleUrl;

    @Value("${map.center-lat:0}")
    private double mapCenterLat;

    @Value("${map.center-lon:0}")
    private double mapCenterLon;

    @Value("${map.zoom:12}")
    private double mapZoom;

    @GetMapping({"/", "/login"})
    public String login() { return "loginView"; }

    @GetMapping({"/dashboard"})
    public String dashboard() { return "dashboardView"; }

    @GetMapping({"/heatmap"})
    public String heatmap(Model model) {
        model.addAttribute("mapStyleUrl", mapStyleUrl);
        model.addAttribute("mapCenterLat", mapCenterLat);
        model.addAttribute("mapCenterLon", mapCenterLon);
        model.addAttribute("mapZoom", mapZoom);
        return "heatmapView";
    }

    @GetMapping({"/reports"})
    public String reports() { return "reportsView"; }

    @GetMapping({"/settings"})
    public String settings() { return "settingsView"; }
}
