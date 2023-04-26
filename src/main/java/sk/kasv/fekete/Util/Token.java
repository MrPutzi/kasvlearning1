package sk.kasv.fekete.Util;

import java.util.HashMap;
import java.util.Map;

public class Token {
    private Token(){  }

    /**7
     * 1. Create a static instance of the class
     * 2. Create a public static method that returns the instance of the class
     */
private static Token tokenManager = new Token();
    public static Token getInstance(){
        return tokenManager;
    }

    /**
     * 1. Create a map to store the tokens
     */

    private Map<String, String> tokens = new HashMap<>();

    /**
     * 1. Create a method to insert a token
     * @param username
     * @param token
     */

    public void insertToken(String username, String token){
        tokens.put(username, token);
    }

    /**
     * 1. Create a method to delete a token
     * @param username
     */

    public void deleteToken(String username){
tokens.remove(username);
    }

    /**
     * 1. Create a method to check if a token is valid
     * @param username
     * @param token
     * @return true if the token is valid, false otherwise
     */

    public boolean checkToken(String username, String token){
    String token2 = tokens.get(username);
    if (token2 == null) {
        return false;
    }
    else {
        return token2.equals(token);
    }


    }







}
