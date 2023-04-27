package sk.kasv.fekete.Util;

import java.util.HashMap;
import java.util.Map;

public class Token {
    // 1. Create a private constructor to prevent external instantiation
    private Token() {}

    // 2. Create a static instance of the class
    private static final Token INSTANCE = new Token();

    // 3. Create a public static method that returns the instance of the class
    public static Token getInstance() {
        return INSTANCE;
    }

    // 4. Create a map to store the tokens
    private final Map<String, String> tokens = new HashMap<>();

    // 5. Create a method to insert a token
    public void insertToken(String username, String token) {
        tokens.put(username, token);
    }

    // 6. Create a method to remove a token
    public void removeToken(String username) {
        tokens.remove(username);
    }

    // 7. Create a method to check if a token is valid
    public boolean validateToken(String username, String token) {
        String storedToken = tokens.get(username);
        return storedToken != null && storedToken.equals(token);
    }

    public void removeToken(String username, String token) {
        String storedToken = tokens.get(username);
        if (storedToken != null && storedToken.equals(token)) {
            tokens.remove(username);
        }
    }

}
