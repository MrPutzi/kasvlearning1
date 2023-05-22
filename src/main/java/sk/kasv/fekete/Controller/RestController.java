package sk.kasv.fekete.Controller;

import com.google.gson.JsonObject;
import com.mongodb.client.*;
import jakarta.annotation.PreDestroy;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.kasv.fekete.Database.DatabaseManager;
import sk.kasv.fekete.Util.Role;
import sk.kasv.fekete.Util.Token;
import sk.kasv.fekete.Util.User;
import sk.kasv.fekete.Util.Util;
import sk.kasv.fekete.Controller.CoursesController;
import java.text.ParseException;
import java.util.*;


@org.springframework.web.bind.annotation.RestController
@CrossOrigin
public class RestController {
    Util util = new Util();
    Map<String, String> tokens = new HashMap<>();
    DatabaseManager databaseManager = new DatabaseManager();
    JSONParser parser = new JSONParser();
    private java.util.Date Date = new java.util.Date(System.currentTimeMillis());

    /**
     * @name: REGISTER
     * @description: This method is used to register a user
     * @author: Roland Fekete
     * @date: 18/05/2023
     **/


    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> register(@RequestBody User user) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("User");
        Document document = new Document()
                .append("username", user.getUsername())
                .append("password", user.getPassword())
                .append("email", user.getEmail())
                .append("role", "USER");
        collection.insertOne(document);
        return ResponseEntity.ok("User registered");
    }

    /**
     * @name: LOGIN
     * @description: This method is used to log in a user and generate a token for him/her
     * @author: Roland Fekete
     * @date: 18/05/2023
     **/

    @CrossOrigin
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<JSONObject> login(@RequestBody String data) throws ParseException {
        try {
            JSONObject response = (JSONObject) parser.parse(data);
            String username = (String) response.get("username");
            String password = (String) response.get("password");
            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase database = mongoClient.getDatabase("Lectures");
            MongoCollection<Document> collection = database.getCollection("User");
            Document document = collection.find(new Document("username", username)).first();
            // if (databaseManager.checkLog(username) != null) {
            //     return new ResponseEntity<>(HttpStatus.BAD_REQUEST).ok(new JSONObject(Map.of("error", "User already logged in with token :  " + tokens.get(username)+" ")));
            //}
            //if (!data.contains("username") || !data.contains("password"))
            //{
            //   return new ResponseEntity<>(HttpStatus.BAD_REQUEST).ok(new JSONObject(Map.of("error", "Bad request")));
            //}
            if (!response.containsKey("username") || !response.containsKey("password")) {
                return ResponseEntity.badRequest().body(new JSONObject(Map.of("error", "Invalid JSON format. 'username' and 'password' fields are required.")));
            }
            if (document != null) {
                if (document.get("password").equals(password)) {
                    String token = util.generateToken(username, password);
                    tokens.put(username, token);
                    DatabaseManager db = new DatabaseManager();
                    db.insertLogWithToken(username, Date, token);
                    Token.getInstance().insertToken(username, token);
                    //databaseManager.insertLogWithToken(username, new Date(), token);
                    return ResponseEntity.ok(new JSONObject(Map.of("token", token, "role", document.get("role"), "username", document.get("username"))));
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JSONObject(Map.of("error", "Invalid password")));
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JSONObject(Map.of("error", "Invalid username")));
            }
        } catch (net.minidev.json.parser.ParseException e) {
            throw new ParseException("Error parsing JSON", 0);
        }
    }

    /**
     * @name: LOGOUT
     * @description: This method is used to log out a user and delete his/her token from the database
     * @author: Roland Fekete
     * @date: 18/05/2023
     **/

    @CrossOrigin
    @PostMapping(value = "/logout/{username}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, String>> logout(@PathVariable String username, @RequestHeader String token) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Username parameter is missing"));
        }
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token header is missing"));
        }
        token = token.substring(7);
        if (!Token.getInstance().validateToken(username, token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
        }
        Token.getInstance().removeToken(username);
        databaseManager.deleteTokenFromLog();
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    /**
     * @name: CHANGE PASSWORD
     * @description: This method is used to change password
     * @author: Roland Fekete
     * @date: 18/05/2023
     **/

    @PostMapping(value = "/changepassword/{username}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> changePassword(@PathVariable String username, @RequestBody User user, @RequestHeader String token) {
        if (token.contains("Bearer")) {
            token = token.substring(7);
        }
        if (Token.getInstance().validateToken(username, token)) {
            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase database = mongoClient.getDatabase("Lectures");
            MongoCollection<Document> collection = database.getCollection("User");
            Document query = new Document("username", username);
            Document userDoc = collection.find(query).first();
            if (userDoc != null) {
                String currentPassword = userDoc.getString("password");
                if (currentPassword.equals(user.getPassword())) {
                    // New password cannot be the same as the current password
                    return ResponseEntity.badRequest().body("New password must be different from current password");
                }
                // Update the password in the user document
                userDoc.put("password", user.getPassword());
                collection.replaceOne(query, userDoc);
                return new ResponseEntity<>("Password changed", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * @name: DESTROY
     * @description: This method is used to clear out the log collection in the database when the server is shut down.
     * @author: Roland Fekete
     * @date: 18/05/2023
     */



}



