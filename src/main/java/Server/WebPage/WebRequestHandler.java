package Server.WebPage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
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
    private static final Path WEB_ROOT = Path.of("src", "main", "java", "Server", "Webpage");
    private static final Path IMAGE_ROOT = Path.of("Images");

    @GetMapping(value = {"/", "/login"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<byte[]> loginPage() throws IOException {
        return fileResponse(WEB_ROOT.resolve("loginView.html"), MediaType.TEXT_HTML);
    }

    @GetMapping(value = "/dashboard", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<byte[]> dashboardPage() throws IOException {
        return fileResponse(WEB_ROOT.resolve("dashboardView.html"), MediaType.TEXT_HTML);
    }

    @GetMapping("/icons/{fileName:.+}")
    public ResponseEntity<byte[]> icon(@PathVariable String fileName) throws IOException {
        return fileResponse(resolveStaticPath(WEB_ROOT.resolve("icons"), fileName), MediaType.valueOf("image/svg+xml"));
    }

    @GetMapping("/Images/{fileName:.+}")
    public ResponseEntity<byte[]> image(@PathVariable String fileName) throws IOException {
        return fileResponse(resolveStaticPath(IMAGE_ROOT, fileName), imageMediaType(fileName));
    }

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

    @GetMapping(value = "/heatmap", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<byte[]> heatmapPage() throws IOException {
        return fileResponse(WEB_ROOT.resolve("heatmapView.html"), MediaType.TEXT_HTML);
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

/*    

    @PostMapping(value = "/login", consumes = PROTO, produces = PROTO)
    public ResponseEntity<ServerToWeb> login(@RequestBody WebToServer request) {
        if (request.getPayloadCase() != WebToServer.PayloadCase.USER_AUTH_REQUEST) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(WebToServerHandler.handleRequest(request));
    }
*/

    private static ResponseEntity<byte[]> fileResponse(Path path, MediaType mediaType) throws IOException {
        Path normalizedPath = path.toAbsolutePath().normalize();
        if (!Files.isRegularFile(normalizedPath)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, mediaType.toString())
                .body(Files.readAllBytes(normalizedPath));
    }

    private static Path resolveStaticPath(Path root, String fileName) throws IOException {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path resolved = normalizedRoot.resolve(fileName).normalize();
        if (!resolved.startsWith(normalizedRoot)) {
            throw new IOException("Invalid static path");
        }
        return resolved;
    }

    private static MediaType imageMediaType(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    public record LoginRequest(String usernameOrEmail, String password) {
    }
}
