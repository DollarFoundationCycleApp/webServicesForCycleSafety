package Server.WebPage;

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

        String view = new WebController().heatmap(model);

        assertEquals("heatmapView", view);
        assertTrue(model.containsAttribute("mapStyleUrl"));
        assertTrue(model.containsAttribute("mapCenterLat"));
        assertTrue(model.containsAttribute("mapCenterLon"));
        assertTrue(model.containsAttribute("mapZoom"));
        assertTrue(model.containsAttribute("incidentLocations"));
    }
}
