package Server.WebPage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import CycleSafety.Server.SCPBV020.FetchIncidentsRequest;
import CycleSafety.Server.SCPBV020.FetchIncidentsResponse;
import CycleSafety.Server.SCPBV020.FetchOffenderHistoryRequest;
import CycleSafety.Server.SCPBV020.FetchOffenderHistoryResponse;
import CycleSafety.Server.SCPBV020.FetchRepeatOffendersRequest;
import CycleSafety.Server.SCPBV020.FetchRepeatOffendersResponse;
import CycleSafety.Server.SCPBV020.HeatmapDataRequest;
import CycleSafety.Server.SCPBV020.HeatmapDataResponse;
import CycleSafety.Server.SCPBV020.Incident;
import CycleSafety.Server.SCPBV020.Location;
import CycleSafety.Server.SCPBV020.RepeatOffenderSummary;
import CycleSafety.Server.SCPBV020.ServerErrorResponse;
import CycleSafety.Server.SCPBV020.ServerToWeb;
import CycleSafety.Server.SCPBV020.UserAuthRequest;
import CycleSafety.Server.SCPBV020.UserAuthResponse;
import CycleSafety.Server.SCPBV020.WebToServer;

@RestController
public class WebRequestHandler {
    private static final String PROTO = "application/x-protobuf";

    @GetMapping(value = "/incidents", produces = PROTO)
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

    @GetMapping(value = "/heatmap", produces = PROTO)
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

    @GetMapping(value = "/repeat-offenders", produces = PROTO)
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
 
    @GetMapping(value = "/offender-history", produces = PROTO)
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

    @PostMapping(value = "/login", consumes = PROTO, produces = PROTO)
    public ResponseEntity<ServerToWeb> login(@RequestBody WebToServer request) {
        if (request.getPayloadCase() != WebToServer.PayloadCase.USER_AUTH_REQUEST) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(WebToServerHandler.handleRequest(request));
    }

}