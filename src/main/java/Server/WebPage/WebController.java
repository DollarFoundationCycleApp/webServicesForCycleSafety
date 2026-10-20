package Server.WebPage;

import Server.Controller.AuthenticationController;
import Server.Model.Incident;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
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

    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a");

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
                    LocalDateTime.of(2026, 9, 24, 12, 5)),
            // Tempe Beach Park
            new Incident(33.4302, -111.9428, "TMP1001", "img-0004", "Minor",
                    true, "Alex Chen, 480-555-0111", "Close pass on the lake path entrance",
                    "Nissan", "Altima", "White", 2018,
                    LocalDateTime.of(2026, 9, 1, 7, 50)),
            new Incident(33.4298, -111.9415, "TMP1002", "img-0005", "None",
                    true, "Maria Lopez, 480-555-0123", "Car rolled through crosswalk",
                    "Chevrolet", "Malibu", "Gray", 2020,
                    LocalDateTime.of(2026, 9, 4, 18, 10)),
            new Incident(33.4306, -111.9436, "TMP1003", "img-0006", "Moderate",
                    false, "", "Hit while turning into parking lot",
                    "Dodge", "Ram 1500", "Red", 2017,
                    LocalDateTime.of(2026, 9, 7, 16, 25)),
            new Incident(33.4295, -111.9422, "TMP1004", "img-0007", "Minor",
                    true, "Tom Becker, 602-555-0134", "Cut off at Rio Salado Pkwy",
                    "Hyundai", "Elantra", "Black", 2022,
                    LocalDateTime.of(2026, 9, 9, 8, 5)),
            new Incident(33.4309, -111.9410, "TMP1005", "img-0008", "None",
                    true, "Priya Patel, 480-555-0145", "Driver honked and swerved toward rider",
                    "Subaru", "Outback", "Green", 2015,
                    LocalDateTime.of(2026, 9, 13, 10, 40)),
            new Incident(33.4300, -111.9440, "TMP1006", "img-0009", "Severe",
                    false, "", "Rider knocked down near the boat ramp",
                    "Jeep", "Wrangler", "Yellow", 2019,
                    LocalDateTime.of(2026, 9, 16, 19, 15)),
            new Incident(33.4304, -111.9418, "TMP1007", "img-0010", "Minor",
                    true, "Kevin Ortiz, 602-555-0156", "Door opened in front of rider",
                    "Kia", "Sorento", "Silver", 2021,
                    LocalDateTime.of(2026, 9, 19, 13, 30)),
            new Incident(33.4297, -111.9431, "TMP1008", "img-0011", "None",
                    true, "Hannah Wu, 480-555-0167", "Near miss at event drop-off",
                    "Tesla", "Model 3", "White", 2023,
                    LocalDateTime.of(2026, 9, 22, 17, 55)),
            new Incident(33.4307, -111.9425, "TMP1009", "img-0012", "Moderate",
                    true, "Derek Hall, 480-555-0178", "Sideswiped leaving the park",
                    "GMC", "Sierra", "Blue", 2016,
                    LocalDateTime.of(2026, 9, 27, 9, 20)),
            // Scottsdale, Eldorado Park
            new Incident(33.4928, -111.9203, "SCT2001", "img-0013", "Minor",
                    true, "Olivia Grant, 480-555-0189", "Clipped on the greenbelt crossing",
                    "BMW", "X5", "Black", 2020,
                    LocalDateTime.of(2026, 9, 2, 6, 45)),
            new Incident(33.4941, -111.9188, "SCT2002", "img-0014", "None",
                    true, "Sam Nguyen, 602-555-0191", "Car blocked bike lane, forced swerve",
                    "Lexus", "RX 350", "White", 2018,
                    LocalDateTime.of(2026, 9, 5, 17, 20)),
            new Incident(33.4933, -111.9210, "SCT2003", "img-0015", "Moderate",
                    false, "", "Hit and run at Miller Rd",
                    "Chevrolet", "Tahoe", "Gray", 2019,
                    LocalDateTime.of(2026, 9, 8, 20, 5)),
            new Incident(33.4946, -111.9199, "SCT2004", "img-0016", "Minor",
                    true, "Rachel Kim, 480-555-0102", "Right hook at park entrance",
                    "Audi", "Q5", "Blue", 2021,
                    LocalDateTime.of(2026, 9, 11, 7, 35)),
            new Incident(33.4922, -111.9192, "SCT2005", "img-0017", "None",
                    true, "Mike Diaz, 602-555-0113", "Driver passed too close on McDowell",
                    "Ford", "Explorer", "Red", 2017,
                    LocalDateTime.of(2026, 9, 15, 12, 50)),
            new Incident(33.4938, -111.9182, "SCT2006", "img-0018", "Severe",
                    true, "Laura Brooks, 480-555-0124", "Rider struck while crossing path",
                    "Toyota", "Tacoma", "Tan", 2022,
                    LocalDateTime.of(2026, 9, 20, 18, 40)),
            new Incident(33.4930, -111.9206, "SCT2007", "img-0019", "Minor",
                    true, "Ben Carter, 602-555-0135", "Mirror contact while merging",
                    "Mazda", "CX-5", "Silver", 2020,
                    LocalDateTime.of(2026, 9, 25, 8, 10)),
            new Incident(33.4944, -111.9194, "SCT2008", "img-0020", "None",
                    false, "", "Aggressive driver near the playground lot",
                    "Volkswagen", "Jetta", "Black", 2014,
                    LocalDateTime.of(2026, 9, 29, 16, 0)));

    @GetMapping({"/", "/login"})
    public String login(@CookieValue(value = "SESSION", required = false) String session) { 
        if (AuthenticationController.isValidSession(session)) {
            return "redirect:/dashboard";
        }
        return "loginView"; }

    @GetMapping({"/dashboard"})
    public String dashboard(@CookieValue(value = "SESSION", required = false) String session, Model model) {
        if (!AuthenticationController.isValidSession(session)) {
            return "redirect:/login";
        }
        model.addAttribute("incidents", SAMPLE_INCIDENTS.stream()
                .sorted(Comparator.comparing(Incident::createdTime).reversed())
                .toList());
        return "dashboardView";
    }

    @GetMapping({"/heatmap"})
    public String heatmap(@CookieValue(value = "SESSION", required = false) String session, Model model) {
        if (!AuthenticationController.isValidSession(session)) {
            return "redirect:/login";
        }
        model.addAttribute("mapStyleUrl", mapStyleUrl);
        model.addAttribute("mapCenterLat", mapCenterLat);
        model.addAttribute("mapCenterLon", mapCenterLon);
        model.addAttribute("mapZoom", mapZoom);
        // Plain values only, so they serialize cleanly into the page script
        model.addAttribute("incidents", SAMPLE_INCIDENTS.stream()
                .map(incident -> Map.ofEntries(
                        Map.entry("latitude", incident.latitude()),
                        Map.entry("longitude", incident.longitude()),
                        Map.entry("licensePlate", incident.licensePlate()),
                        Map.entry("imageId", incident.imageId()),
                        Map.entry("injurySeverity", incident.injurySeverity()),
                        Map.entry("driverPresent", incident.driverPresent()),
                        Map.entry("driverInformation", incident.driverInformation()),
                        Map.entry("extraComment", incident.extraComment()),
                        Map.entry("vehicleMake", incident.vehicleMake()),
                        Map.entry("vehicleModel", incident.vehicleModel()),
                        Map.entry("vehicleColor", incident.vehicleColor()),
                        Map.entry("vehicleYear", incident.vehicleYear()),
                        Map.entry("createdTime", incident.createdTime().format(DISPLAY_TIME))))
                .toList());
        return "heatmapView";
    }

    @GetMapping({"/incident"})
    public String incident(@CookieValue(value = "SESSION", required = false) String session, Model model) {
        if (!AuthenticationController.isValidSession(session)) {
            return "redirect:/login";
        }
        model.addAttribute("incidents", SAMPLE_INCIDENTS);
        return "incidentView";
    }

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
