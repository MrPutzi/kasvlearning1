package sk.kasv.fekete.Util;

import java.util.HashMap;
import java.util.Map;

public class Token {
    private Token() {}
    private static final Token INSTANCE = new Token();
    public static Token getInstance() {
        return INSTANCE;
    }
    private final Map<String, String> tokens = new HashMap<>();
    public void insertToken(String username, String token) {
        tokens.put(username, token);
    }
    public void removeToken(String username) {
        tokens.remove(username);
    }
    public boolean validateToken(String username) {
        return tokens.containsKey(username);
    }
    public String validateUsername(String token) {
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            if (entry.getValue().equals(token)) {
                return entry.getKey(); // Return the username associated with the token
            }
        }
        return null; // Token not found
    }

    public void removeToken(String username, String token) {
        String storedToken = tokens.get(username);
        if (storedToken != null && storedToken.equals(token)) {
            tokens.remove(username);
        }
    }
}
