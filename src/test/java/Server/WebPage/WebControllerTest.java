package Server.WebPage;

import Server.Controller.AuthenticationController;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import static org.junit.jupiter.api.Assertions.*;

public class WebControllerTest {

    /**
     * Heatmap page returns its view and passes the map settings to it.
     * Runs without starting the server.
     */
    @Test
    public void heatmapProvidesMapSettings() {
        ExtendedModelMap model = new ExtendedModelMap();

        String session = AuthenticationController.authenticateUser("admin", "password");

        String view = new WebController(null).heatmap(session, model);

        assertEquals("heatmapView", view);
        assertTrue(model.containsAttribute("mapStyleUrl"));
        assertTrue(model.containsAttribute("mapCenterLat"));
        assertTrue(model.containsAttribute("mapCenterLon"));
        assertTrue(model.containsAttribute("mapZoom"));
        assertTrue(model.containsAttribute("incidents"));
    }

    /**
     * The create account page is reachable while logged out and bounces
     * authenticated users back to the dashboard.
     */
    @Test
    public void registerShowsViewWhenLoggedOut() {
        WebController controller = new WebController(null);

        assertEquals("registerView", controller.register(null));

        String session = AuthenticationController.authenticateUser("admin", "password");
        assertEquals("redirect:/dashboard", controller.register(session));
    }
}
