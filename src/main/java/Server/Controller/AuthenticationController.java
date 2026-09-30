package Server;

import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class AuthenticationController {
    private static final ConcurrentHashMap<String, String> activeSessions = new ConcurrentHashMap<>();

    private static final String MOCK_USER = "admin";
    private static final String MOCK_PASSWORD_HASH = hashPassword("password");

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String authenticateUser(String username, String password) {
        if (MOCK_USER.equals(username) && MOCK_PASSWORD_HASH.equals(hashPassword(password))) {
            String sessionId = UUID.randomUUID().toString();
            activeSessions.put(sessionId, username);
            return sessionId;
        }
        return null;
    }

    public static boolean isValidSession(String sessionId) {
        return sessionId != null && activeSessions.containsKey(sessionId);
    }

    public static boolean logout(String sessionId) {
        if (sessionId != null) {
            activeSessions.remove(sessionId);
            return true;
        }
        return false;
    }
}