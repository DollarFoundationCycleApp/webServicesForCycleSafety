package Server.Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import CycleSafety.Server.SCPBV020.Incident;
import CycleSafety.Server.SCPBV020.IncidentPhoto;
import CycleSafety.Server.SCPBV020.Location;
import CycleSafety.Server.SCPBV020.OffenderVehicle;

/**
 * DatabaseCommunicationContext provides JDBC operations for storing and retrieving
 * incident data, images, and user information.
 *
 * Incident information and incident images live in two separate tables
 * ({@code incidents} and {@code incident_photos}) that are linked by
 * {@code device_event_id}.
 */
public class DatabaseCommunicationContext {

    private final String dbHost;
    private final int dbPort;
    private final String dbName;
    private final String dbUser;
    private final String dbPassword;

    public DatabaseCommunicationContext(String host, int port, String name, String user, String password) {
        this.dbHost = host;
        this.dbPort = port;
        this.dbName = name;
        this.dbUser = user;
        this.dbPassword = password;
    }

    /**
     * Open a new database connection. Callers are responsible for closing it.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getJdbcUrl(), dbUser, dbPassword);
    }

    private String getJdbcUrl() {
        return "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName
                + "?useSSL=false&serverTimezone=UTC";
    }

    /**
     * Store an incident, its linked photos, and the optional offender vehicle in a
     * single transaction. The photos are linked to the incident via device_event_id.
     *
     * @return the server-assigned incident_id on success, 0 on failure.
     */
    public long storeIncident(String userId, Incident incident, List<IncidentPhoto> photos,
                              OffenderVehicle vehicle) {
        String incidentSql = "INSERT INTO incidents (device_event_id, user_id, timestamp_ms, "
                + "latitude, longitude, accuracy_meters, distance_cm, time_offset_ms, is_anonymized) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String photoSql = "INSERT INTO incident_photos (device_event_id, image_index, format, "
                + "image_data, contains_sensitive_data) VALUES (?, ?, ?, ?, ?)";
        String vehicleSql = "INSERT INTO offender_vehicles (device_event_id, license_plate, "
                + "state_or_region, vehicle_make, vehicle_model, vehicle_color) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            int deviceEventId = incident.getDeviceEventId();
            Location location = incident.getLocation();

            long incidentId;
            try (PreparedStatement ps = conn.prepareStatement(incidentSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, deviceEventId);
                ps.setString(2, userId);
                ps.setLong(3, incident.getTimestampMs());
                ps.setDouble(4, location.getLatitude());
                ps.setDouble(5, location.getLongitude());
                ps.setFloat(6, location.getAccuracyMeters());
                ps.setInt(7, incident.getDistanceCm());
                ps.setInt(8, incident.getTimeOffsetMs());
                ps.setBoolean(9, incident.getIsAnonymized());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No incident_id returned for device_event_id " + deviceEventId);
                    }
                    incidentId = keys.getLong(1);
                }
            }

            if (photos != null && !photos.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(photoSql)) {
                    for (IncidentPhoto photo : photos) {
                        ps.setInt(1, deviceEventId);
                        ps.setInt(2, photo.getImageIndex());
                        ps.setString(3, photo.getFormat());
                        ps.setBytes(4, photo.getImageData().toByteArray());
                        ps.setBoolean(5, photo.getContainsSensitiveData());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            if (vehicle != null && hasVehicleData(vehicle)) {
                try (PreparedStatement ps = conn.prepareStatement(vehicleSql)) {
                    ps.setInt(1, deviceEventId);
                    ps.setString(2, vehicle.getLicensePlate());
                    ps.setString(3, vehicle.getStateOrRegion());
                    ps.setString(4, vehicle.getVehicleMake());
                    ps.setString(5, vehicle.getVehicleModel());
                    ps.setString(6, vehicle.getVehicleColor());
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return incidentId;
        } catch (SQLException e) {
            System.err.println("Error storing incident: " + e.getMessage());
            rollbackQuietly(conn);
            return 0;
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Store a single incident photo linked to an incident by device_event_id.
     */
    public boolean storeIncidentPhoto(int deviceEventId, int imageIndex, String format,
                                      byte[] imageData, boolean containsSensitiveData) {
        String sql = "INSERT INTO incident_photos (device_event_id, image_index, format, "
                + "image_data, contains_sensitive_data) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deviceEventId);
            ps.setInt(2, imageIndex);
            ps.setString(3, format);
            ps.setBytes(4, imageData);
            ps.setBoolean(5, containsSensitiveData);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error storing incident photo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all incidents for a specific user.
     */
    public List<Map<String, Object>> getIncidentsByUser(String userId, int limit, int offset) {
        List<Map<String, Object>> incidents = new ArrayList<>();

        String sql = "SELECT incident_id, device_event_id, user_id, timestamp_ms, latitude, "
                + "longitude, accuracy_meters, distance_cm, time_offset_ms, is_anonymized "
                + "FROM incidents WHERE user_id = ? ORDER BY timestamp_ms DESC LIMIT ? OFFSET ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> incident = new HashMap<>();
                    incident.put("incident_id", rs.getLong("incident_id"));
                    incident.put("device_event_id", rs.getInt("device_event_id"));
                    incident.put("user_id", rs.getString("user_id"));
                    incident.put("timestamp_ms", rs.getLong("timestamp_ms"));
                    incident.put("latitude", rs.getDouble("latitude"));
                    incident.put("longitude", rs.getDouble("longitude"));
                    incident.put("accuracy_meters", rs.getFloat("accuracy_meters"));
                    incident.put("distance_cm", rs.getInt("distance_cm"));
                    incident.put("time_offset_ms", rs.getInt("time_offset_ms"));
                    incident.put("is_anonymized", rs.getBoolean("is_anonymized"));
                    incidents.add(incident);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching incidents: " + e.getMessage());
        }

        return incidents;
    }

    /**
     * Get all incident images for a specific device event ID.
     */
    public List<Map<String, Object>> getIncidentPhotos(int deviceEventId) {
        List<Map<String, Object>> photos = new ArrayList<>();

        String sql = "SELECT image_index, format, image_data, contains_sensitive_data "
                + "FROM incident_photos WHERE device_event_id = ? ORDER BY image_index";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, deviceEventId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> photo = new HashMap<>();
                    photo.put("image_index", rs.getInt("image_index"));
                    photo.put("format", rs.getString("format"));
                    photo.put("photo_data", rs.getBytes("image_data"));
                    photo.put("contains_sensitive_data", rs.getBoolean("contains_sensitive_data"));
                    photos.add(photo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching incident photos: " + e.getMessage());
        }

        return photos;
    }

    private static boolean hasVehicleData(OffenderVehicle vehicle) {
        return !vehicle.getLicensePlate().isEmpty()
                || !vehicle.getStateOrRegion().isEmpty()
                || !vehicle.getVehicleMake().isEmpty()
                || !vehicle.getVehicleModel().isEmpty()
                || !vehicle.getVehicleColor().isEmpty();
    }

    private static void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
                // nothing we can do here
            }
        }
    }

    private static void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
                // nothing we can do here
            }
        }
    }
}
