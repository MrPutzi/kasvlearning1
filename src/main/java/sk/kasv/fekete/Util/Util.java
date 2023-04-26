package sk.kasv.fekete.Util;

import java.util.regex.Pattern;

public class Util {

    /**
     * 1. Create a method to check if a string is a valid email
     * @param username
     * @param password
     * @return
     */

    public String generateToken(String username, String password){
String token = username + password;
if (token.contains("a")) token = token.replace("a", "1");
if (token.contains("e")) token = token.replace("e", "2");
if (token.contains("i")) token = token.replace("i", "3");
if (token.contains("o")) token = token.replace("o", "4");
if (token.contains("u")) token = token.replace("u", "5");
return token;
    }

}
