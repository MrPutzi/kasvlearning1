package sk.kasv.fekete.Controller;

import com.mongodb.client.*;
import org.bson.Document;
import org.bson.json.JsonObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.kasv.fekete.Database.DatabaseManager;
import sk.kasv.fekete.Util.Lecture;
import sk.kasv.fekete.Util.Token;
import sk.kasv.fekete.Util.User;
import sk.kasv.fekete.Util.Util;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@org.springframework.web.bind.annotation.RestController
@CrossOrigin
public class RestController {

    Map<String, String> tokens = new HashMap<>();
    Util util = new Util();
    DatabaseManager databaseManager = new DatabaseManager();

    /**
     * @author: Roland Fekete
     * @date: 2023.04.26
     * @description: This method is used to register a user
     */


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
     * @author: Roland Fekete
     * @date: 2023.04.26
     * @description: This method is used to log in a user
     * @if user exists and password is correct, return role constant
     */
    @CrossOrigin
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> login(@RequestBody User user) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("User");
        Document query = new Document("username", user.getUsername()).append("password", user.getPassword());
        FindIterable<Document> iterable = collection.find(query);
        Iterator<Document> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            Document document = iterator.next();
            String role = document.getString("role");
            String token = new Util().generateToken(user.getUsername(), user.getPassword());
            Token.getInstance().insertToken(user.getUsername(), token);
            databaseManager.insertLogWithToken(user.getUsername(), new Date(), token);
            if (role.equals("ADMIN")) {
                return ResponseEntity.status((HttpStatus.OK)).body(token);
            } else if (role.equals("USER")) {
                return ResponseEntity.status(HttpStatus.OK).body(token);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unknown role");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    /**
     * @author: Roland Fekete
     * @date: 2023.04.26
     * @description: This method is used to log out a user
     */
    @PostMapping(value = "/logout/{username}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, String>> logout(@PathVariable String username, @RequestHeader String token) {
        if (token.contains("Bearer")) {
            token = token.substring(7);
        }
        if (Token.getInstance().validateToken(username, token)) {
            Token.getInstance().removeToken(username, token);
            return ResponseEntity.ok(Map.of("message", "Logout successful"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
        }
    }
    /**
     * @author: Roland Fekete
     * @date: 2023.04.28
     * @description: This method is used to change password
     */

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
}

