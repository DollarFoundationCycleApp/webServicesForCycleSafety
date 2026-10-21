package Server.Model;

import java.time.LocalDateTime;

public record Incident(
        String incidentId,
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
        LocalDateTime createdTime,
        boolean hideInfo) {

        public static Incident hideInfo(Incident incident) {
                boolean hidden = incident.hideInfo();
                return new Incident(
                        incident.incidentId(), incident.latitude(), incident.longitude(),
                        hidden ? null : incident.licensePlate(), incident.imageId(),
                        incident.injurySeverity(), incident.driverPresent(),
                        hidden ? null : incident.driverInformation(), hidden ? null : incident.extraComment(),
                        incident.vehicleMake(), incident.vehicleModel(), incident.vehicleColor(),
                        incident.vehicleYear(), incident.createdTime(), incident.hideInfo());
        }

}
