package Server.WebPage;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import CycleSafety.Server.SCPBV020.FetchIncidentsRequest;
import CycleSafety.Server.SCPBV020.FetchOffenderHistoryRequest;
import CycleSafety.Server.SCPBV020.FetchRepeatOffendersRequest;
import CycleSafety.Server.SCPBV020.HeatmapDataRequest;
import CycleSafety.Server.SCPBV020.ServerToWeb;
import CycleSafety.Server.SCPBV020.WebToServer;
import Server.Controller.AuthenticationController;

// Handles the different request endpoints from the web
@RestController
public class WebRequestHandler {
    private static final String PROTO = "application/x-protobuf";
    
    @PostMapping(value = "/api/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> loginJson(@RequestBody LoginRequest request) {
        String sessionToken = AuthenticationController.authenticateUser(
                request.usernameOrEmail(),
                request.password()
        );

        if (sessionToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "authenticated", false,
                            "message", "Invalid credentials"
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "userId", request.usernameOrEmail(),
                "sessionToken", sessionToken,
                "isAdmin", true
        ));
    }


    @GetMapping(value = "/api/incidents", produces = PROTO)
    public ServerToWeb incidents(
        @RequestParam(defaultValue = "0") long userId,
        @RequestParam(defaultValue = "50") int limit,
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "false") boolean isAnonymized) {

            return WebToServerHandler.handleRequest(WebToServer.newBuilder()
                .setFetchIncidentsRequest(FetchIncidentsRequest.newBuilder()
                    .setUserId(userId)
                    .setLimit(limit)
                    .setOffset(offset)
                    .setIncludeAnonymizedOnly(isAnonymized)
                    .build())
                .build());
    }

    @GetMapping(value = "/api/heatmap", produces = PROTO)
    public ServerToWeb heatmap(
        @RequestParam(defaultValue = "-90") double minLat,
        @RequestParam(defaultValue = "90") double maxLat,
        @RequestParam(defaultValue = "-180") double minLong,
        @RequestParam(defaultValue = "180") double maxLong,
        @RequestParam(defaultValue = "50") long start,
        @RequestParam(required = false) Long end) {


            return WebToServerHandler.handleRequest(WebToServer.newBuilder()
                .setHeatmapDataRequest(HeatmapDataRequest.newBuilder()
                    .setMinLatitude(minLat)
                    .setMaxLatitude(maxLat)
                    .setMinLongitude(minLong)
                    .setMaxLongitude(maxLong)
                    .setStartTimestampMs(start)
                    .setEndTimestampMs(end != null ? end : System.currentTimeMillis())
                    .build())
                .build());
    }

    @GetMapping(value = "/api/repeat-offenders", produces = PROTO)
    public ServerToWeb repeatOffenders(
        @RequestParam(defaultValue = "2") int minIncidents,
        @RequestParam(defaultValue = "50") int limit,
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "") String state) {
        
            return WebToServerHandler.handleRequest(WebToServer.newBuilder()
                .setFetchRepeatOffendersRequest(FetchRepeatOffendersRequest.newBuilder()
                    .setMinIncidentCount(minIncidents)
                    .setLimit(limit)
                    .setOffset(offset)
                    .setStateOrRegionFilter(state))
                .build());
    }
 
    @GetMapping(value = "/api/offender-history", produces = PROTO)
    public ServerToWeb offenderHistory(
        @RequestParam(defaultValue = "") String plate,
        @RequestParam(defaultValue = "") String state,
        @RequestParam(defaultValue = "50") int limit,
        @RequestParam(defaultValue = "0") int offset) {
        
            return WebToServerHandler.handleRequest(WebToServer.newBuilder()
                .setFetchOffenderHistoryRequest(FetchOffenderHistoryRequest.newBuilder()
                    .setLicensePlate(plate)
                    .setStateOrRegion(state)
                    .setLimit(limit)
                    .setOffset(offset))
                .build());
    }

    public record LoginRequest(String usernameOrEmail, String password) {
    }
}
