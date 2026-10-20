package Server.WebPage;

import Server.Model.Incident;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

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

    // Demo data until incidents come from the app
    private static final List<Incident> SAMPLE_INCIDENTS = List.of(
            new Incident(33.4310, -111.9370, "ABC1234", "img-0001", "Minor",
                    true, "Jane Smith, 602-555-0142", "Clipped by side mirror while passing",
                    "Toyota", "Camry", "Silver", 2019,
                    LocalDateTime.of(2026, 9, 3, 8, 15)),
            new Incident(33.4935, -111.9195, "XYZ9876", "img-0002", "Moderate",
                    false, "", "Driver left the scene heading north",
                    "Ford", "F-150", "Black", 2021,
                    LocalDateTime.of(2026, 9, 12, 17, 42)),
            new Incident(33.4210, -112.0735, "LMN4567", "img-0003", "None",
                    true, "Carlos Ruiz, 480-555-0199", "Door opened into bike lane",
                    "Honda", "Civic", "Blue", 2016,
                    LocalDateTime.of(2026, 9, 24, 12, 5)));

    @GetMapping({"/", "/login"})
    public String login() { return "loginView"; }

    @GetMapping({"/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("incidents", SAMPLE_INCIDENTS.stream()
                .sorted(Comparator.comparing(Incident::createdTime).reversed())
                .toList());
        return "dashboardView";
    }

    @GetMapping({"/heatmap"})
    public String heatmap(Model model) {
        model.addAttribute("mapStyleUrl", mapStyleUrl);
        model.addAttribute("mapCenterLat", mapCenterLat);
        model.addAttribute("mapCenterLon", mapCenterLon);
        model.addAttribute("mapZoom", mapZoom);
        return "heatmapView";
    }

    @GetMapping({"/incident"})
    public String incident(Model model) {
        model.addAttribute("incidents", SAMPLE_INCIDENTS);
        return "incidentView";
    }

    @GetMapping({"/reports"})
    public String reports() { return "reportsView"; }

    @GetMapping({"/settings"})
    public String settings() { return "settingsView"; }
}
