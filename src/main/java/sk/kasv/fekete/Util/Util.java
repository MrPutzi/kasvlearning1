package sk.kasv.fekete.Util;

import java.util.UUID;
import java.util.regex.Pattern;

public class Util {

    /**
     * 1. Create a method to check if a string is a valid email
     * @param username
     * @param password
     * @return
     */

    public String generateToken(String username, String password){
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

}
