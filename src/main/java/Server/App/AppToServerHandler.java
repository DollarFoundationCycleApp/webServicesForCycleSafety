package Server.App;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import CycleSafety.Server.SCPBV020.*;
import CycleSafety.Server.SCPBV020.AppToServer;
import CycleSafety.Server.SCPBV020.Incident;
import CycleSafety.Server.SCPBV020.IncidentPhoto;
import CycleSafety.Server.SCPBV020.OffenderVehicle;
import CycleSafety.Server.SCPBV020.ServerErrorResponse;
import CycleSafety.Server.SCPBV020.ServerToApp;
import CycleSafety.Server.SCPBV020.SubmitIncidentRequest;
import CycleSafety.Server.SCPBV020.SubmitIncidentResponse;
import CycleSafety.Server.SCPBV020.UserAuthRequest;
import CycleSafety.Server.SCPBV020.UserAuthResponse;

public class AppToServerHandler {

    // Handles the different requests from the app client and returns the appropriate response
    public static void handleAppToServer(InputStream in, OutputStream out, int clientID) {
        AppToServer request;
        try {        
            while ((request = AppToServer.parseDelimitedFrom(in)) != null) {
                ServerToApp.Builder response = ServerToApp.newBuilder();

                switch (request.getPayloadCase()) {
                    case SUBMIT_INCIDENT_REQUEST:
                        SubmitIncidentRequest submitRequest = request.getSubmitIncidentRequest();
                        response = handleIncidentSubmission(submitRequest);
                        break;
                    case USER_AUTH_REQUEST:
                        UserAuthRequest authRequest = request.getUserAuthRequest();
                        response = handleUserAuthentication(authRequest);
                        break;
                    default:
                        response.setErrorResponse(ServerErrorResponse.newBuilder()
                                .setErrorCode(400)
                                .setErrorMessage("Unknown request type")
                                .build())
                                .build();
                        break;
                }
                    response.build().writeDelimitedTo(out);
                    out.flush();
            }
        } catch (Exception e) {
            System.err.println("Error processing request from client " + clientID + ": " + e.getMessage());
        }
    }

    // Handles SubmitIncidentRequest and returns a SubmitIncidentResponse
    private static ServerToApp.Builder handleIncidentSubmission(SubmitIncidentRequest request) {
        ServerToApp.Builder response = ServerToApp.newBuilder();

        String UserID = request.getUserId();
        Incident incident = request.getIncident();
        List<IncidentPhoto> photos = request.getPhotosList();
        OffenderVehicle offenderVehicle = request.getVehicleInfo();

        //TO_DO: Implement logic to process the incident

        response.setSubmitIncidentResponse(SubmitIncidentResponse.newBuilder()
                .setSuccess(true)
                .setServerIncidentId(1)
                .setMessage("Incident submitted successfully")
                .setTimestampMs(System.currentTimeMillis())
                .build());

        return response;
    }

    // Handles UserAuthRequest and returns a UserAuthResponse
    private static ServerToApp.Builder handleUserAuthentication(UserAuthRequest request) {
        ServerToApp.Builder response = ServerToApp.newBuilder();

        String username = request.getUsernameOrEmail();
        String token = request.getAuthTokenOrHash();


        //TO_DO: Implement logic to authenticate the user

        response.setUserAuthResponse(UserAuthResponse.newBuilder()
                .setAuthenticated(true)
                .setUserId(username)
                .setSessionToken("123")
                .setIsAdmin(true));

        return response;
    }
}

