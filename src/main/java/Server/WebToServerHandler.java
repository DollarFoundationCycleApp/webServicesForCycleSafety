package Server;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

import CycleSafety.Server.SCPBV020.FetchIncidentsRequest;
import CycleSafety.Server.SCPBV020.FetchIncidentsResponse;
import CycleSafety.Server.SCPBV020.FetchOffenderHistoryRequest;
import CycleSafety.Server.SCPBV020.FetchOffenderHistoryResponse;
import CycleSafety.Server.SCPBV020.FetchRepeatOffendersRequest;
import CycleSafety.Server.SCPBV020.FetchRepeatOffendersResponse;
import CycleSafety.Server.SCPBV020.HeatmapDataRequest;
import CycleSafety.Server.SCPBV020.HeatmapDataResponse;
import CycleSafety.Server.SCPBV020.RepeatOffenderSummary;
import CycleSafety.Server.SCPBV020.ServerErrorResponse;
import CycleSafety.Server.SCPBV020.ServerToWeb;
import CycleSafety.Server.SCPBV020.UserAuthRequest;
import CycleSafety.Server.SCPBV020.UserAuthResponse;
import CycleSafety.Server.SCPBV020.WebToServer;

public class WebToServerHandler {
    


    public static void handleWebToServer(InputStream in, OutputStream out, int clientID) {
        WebToServer request;
        try {

        
            while ((request = WebToServer.parseDelimitedFrom(in)) != null) {
                ServerToWeb.Builder response = ServerToWeb.newBuilder();

                switch (request.getPayloadCase()) {
                    case FETCH_INCIDENTS_REQUEST:
                        FetchIncidentsRequest fetchRequest = request.getFetchIncidentsRequest();
                        response = handleFetchIncidents(fetchRequest); 
                        break;

                    case USER_AUTH_REQUEST:
                        UserAuthRequest authRequest = request.getUserAuthRequest();
                        response = handleUserAuthentication(authRequest);
                        break;
                    
                    case HEATMAP_DATA_REQUEST:
                        HeatmapDataRequest heatmapRequest = request.getHeatmapDataRequest();
                        response = handleHeatmapDataRequest(heatmapRequest);
                        break;

                    case FETCH_REPEAT_OFFENDERS_REQUEST:
                        FetchRepeatOffendersRequest repeatOffendersRequest = request.getFetchRepeatOffendersRequest();
                        response = handleFetchRepeatOffenders(repeatOffendersRequest);
                        break;

                    case FETCH_OFFENDER_HISTORY_REQUEST:
                        FetchOffenderHistoryRequest historyRequest = request.getFetchOffenderHistoryRequest();
                        response = handleFetchOffenderHistory(historyRequest);
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

    private static ServerToWeb.Builder handleFetchIncidents(FetchIncidentsRequest request) {
        ServerToWeb.Builder response = ServerToWeb.newBuilder();

        long userId = request.getUserId();
        int limit = request.getLimit();
        int offset = request.getOffset();
        boolean anonymized = request.getIncludeAnonymizedOnly();


        //TO_DO: Implement logic to fetch incidents

        response.setFetchIncidentsResponse(FetchIncidentsResponse.newBuilder()
                .addAllIncidents(Collections.emptyList())     // Replace with the actual list of incidents
                .setTotalCount(0)           // Replace 0 with the actual total count of incidents
                .build());                        
        return response;
    }

    private static ServerToWeb.Builder handleUserAuthentication(UserAuthRequest request) {
        ServerToWeb.Builder response = ServerToWeb.newBuilder();

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

    private static ServerToWeb.Builder handleHeatmapDataRequest(HeatmapDataRequest request) {
        ServerToWeb.Builder response = ServerToWeb.newBuilder();

        double minLatitude = request.getMinLatitude();
        double maxLatitude = request.getMaxLatitude();
        double minLongitude = request.getMinLongitude();
        double maxLongitude = request.getMaxLongitude();
        long startTime = request.getStartTimestampMs();
        long endTime = request.getEndTimestampMs();

        //TO_DO: Implement logic to fetch heatmap data

        response.setHeatmapDataResponse(HeatmapDataResponse.newBuilder()
                .addAllPoints(Collections.emptyList())
                .build());

        return response;
    }

    private static ServerToWeb.Builder handleFetchRepeatOffenders(FetchRepeatOffendersRequest request) {
        ServerToWeb.Builder response = ServerToWeb.newBuilder();

        int minIncidents = request.getMinIncidentCount();
        int limit = request.getLimit();
        int offset = request.getOffset();
        String state = request.getStateOrRegionFilter();

        //TO_DO: Implement logic to fetch repeat offenders

        response.setFetchRepeatOffendersResponse(FetchRepeatOffendersResponse.newBuilder()
                .addAllOffenders(Collections.emptyList())     // Replace with the actual list of repeat offenders
                .setTotalCount(0)           // Replace 0 with the actual total count of repeat offenders
                .build());

        return response;
    }

    private static ServerToWeb.Builder handleFetchOffenderHistory(FetchOffenderHistoryRequest request) {
        ServerToWeb.Builder response = ServerToWeb.newBuilder();

        String licensePlate = request.getLicensePlate();
        String state = request.getStateOrRegion();
        int limit = request.getLimit();
        int offset = request.getOffset();

        //TO_DO: Implement logic to fetch offender history

        response.setFetchOffenderHistoryResponse(FetchOffenderHistoryResponse.newBuilder()
                .setOffenderInfo(RepeatOffenderSummary.newBuilder()
                        .setLicensePlate(licensePlate)
                        .setStateOrRegion(state)
                        .setTotalIncidents(0)
                        .setFirstSeenTimestampMs(0)
                        .setLastSeenTimestampMs(0)
                        .setVehicleMake("Unknown")
                        .setVehicleModel("Unknown")
                        .setVehicleColor("Unknown")
                        .build())
                .addAllIncidents(Collections.emptyList())
                .build());

        return response;
    }
}
