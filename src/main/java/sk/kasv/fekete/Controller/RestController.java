package sk.kasv.fekete.Controller;

import com.google.gson.JsonObject;
import com.mongodb.client.*;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.kasv.fekete.Database.DatabaseManager;
import sk.kasv.fekete.Util.Token;
import sk.kasv.fekete.Util.User;
import sk.kasv.fekete.Util.Util;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.joda.time.format.ISODateTimeFormat.date;


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
     * @description: This method is used to log in a user and generate a token for user
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
            if (!response.containsKey("username") || !response.containsKey("password")) {
                return ResponseEntity.badRequest().body(new JSONObject(Map.of("error", "Invalid JSON format. 'username' and 'password' fields are required.")));
            }
            if (document != null) {
                if (document.get("password").equals(password)) {
                    String token = util.generateToken(username, password);
                    tokens.put(username, token);
                    DatabaseManager db = new DatabaseManager();
                    //db.handleRequest((jakarta.servlet.http.HttpServletRequest) request,username, date());
                    Token.getInstance().insertToken(username, token);
                    //databaseManager.insertLogWithToken(username, new Date(), token);
                    return ResponseEntity.ok(new JSONObject(Map.of("token", token, "role", document.get("role"), "username", document.get("username"))));
                } else {
                    databaseManager.insertLogWithToken(Date, username);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JSONObject(Map.of("error", "Invalid password")));
                }
            } else {
                databaseManager.insertLogWithToken(Date, username); 
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
    @PostMapping(value = "/logout")
    @ResponseBody
    public ResponseEntity<Object> logout(@RequestHeader("token") String token) {
        if (token.contains("Bearer")) {
            token = token.substring(7);
        }
        String username = Token.getInstance().validateUsername(token);

        if (username == null) {
            JsonObject response = new JsonObject();
            response.addProperty("error", "Invalid token.");
            return ResponseEntity.badRequest().body(response.toString());
        }

        Token.getInstance().removeToken(token);
        databaseManager.deleteTokenFromLog();
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }


    /**
     * @name: CHANGE PASSWORD
     * @description: This method is used to change password
     * @author: Roland Fekete
     * @date: 18/05/2023
     **/

    @CrossOrigin
    @PostMapping(value = "/changepassword", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Object> changePassword(@RequestHeader("token") String token, @RequestBody String data) throws ParseException {
        if (token.contains("Bearer")) {
            token = token.substring(7);
        }
        String username = Token.getInstance().validateUsername(token);
        JSONObject response = null;
        try {
            response = (JSONObject) parser.parse(data);
        } catch (net.minidev.json.parser.ParseException e) {
            throw new RuntimeException(e);
        }
        String oldPassword = (String) response.get("oldPassword");
        String newPassword = (String) response.get("newPassword");
        if (username == null) {
            return ResponseEntity.badRequest().body(new JSONObject(Map.of("error", "Invalid token.")));
        }
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("User");
        Document document = collection.find(new Document("username", username)).first();
        if (document.get("password").equals(oldPassword)) {
            databaseManager.changePassword(username, oldPassword, newPassword);
            return ResponseEntity.ok().body(new JSONObject(Map.of("message", "Password changed")));
        } else {
            return ResponseEntity.badRequest().body(new JSONObject(Map.of("error", "Invalid password")));
        }
    }

    /**
     * @name: DELETE USER
     * @description: This method is used to delete a user from the database by the admin user with admin role. It is not allowed to delete an admin user with admin role
     * @date: 18/05/2023
     **/

    @CrossOrigin
    @DeleteMapping(value = "/deleteuser", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Object> deleteUser(@RequestHeader("token") String token, @RequestBody String data) throws ParseException {
        if (token.contains("Bearer")) {
            token = token.substring(7);
        }
        String username = Token.getInstance().validateUsername(token);
        JSONObject response = null;
        try {
            response = (JSONObject) parser.parse(data);
        } catch (net.minidev.json.parser.ParseException e) {
            throw new RuntimeException(e);
        }
        String userToDelete = (String) response.get("userToDelete");
        if (username == null) {
            return ResponseEntity.badRequest().body(new JSONObject(Map.of("error", "Invalid token.")));
        }
        if (username.equals("admin")) {
            if (userToDelete.equals("admin")) {
                return ResponseEntity.badRequest().body(new JSONObject(Map.of("error", "You cannot delete admin.")));
            } else {
                databaseManager.deleteUser(userToDelete);
                return ResponseEntity.ok().body(new JSONObject(Map.of("message", "User deleted")));
            }
        }
        return ResponseEntity.badRequest().body(new JSONObject(Map.of("error", "You are not admin.")));
    }

    @CrossOrigin
    @GetMapping("/test/log")
    @ResponseBody
    public ResponseEntity<Object> getLog() {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("Log");
        FindIterable<Document> iterDoc = collection.find();
        Iterator it = iterDoc.iterator();
        JSONArray array = new JSONArray();
        while (it.hasNext()) {
            array.add(it.next());
        }
        return ResponseEntity.ok(array);
    }


}

