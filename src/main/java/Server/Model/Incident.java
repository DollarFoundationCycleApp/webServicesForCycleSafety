package Server.Model;

import java.time.LocalDateTime;

public record Incident(
        double latitude,
        double longitude,
        String licensePlate,
        String imageId,
        String injurySeverity,
        boolean driverPresent,
        String driverInformation,
        String extraComment,
        String vehicleMake,
        String vehicleModel,
        String vehicleColor,
        int vehicleYear,
        LocalDateTime createdTime) {
}
