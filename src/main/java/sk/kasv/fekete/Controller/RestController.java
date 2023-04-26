package sk.kasv.fekete.Controller;

import com.mongodb.client.*;
import org.bson.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.kasv.fekete.Database.getConnection;
import sk.kasv.fekete.Util.Role;
import sk.kasv.fekete.Util.Token;
import sk.kasv.fekete.Util.User;
import sk.kasv.fekete.Util.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@org.springframework.web.bind.annotation.RestController
@CrossOrigin
public class RestController {

    Map<String,String> tokens = new HashMap<>();
    Util util = new Util();

    /*@PostMapping(name = "/login")
    public String login() {
        String username="";
        String password="";
        getConnection connection = new getConnection();
        Role role = connection.checkUser(username, password);
        if (role == null) {
            return "Wrong username or password";
        } else {
            String token = new Util().generateToken(username, password);
            Token.getInstance().insertToken(username, token);
        }
        return "Login successful";
    }
    */



 /*   @PostMapping (value = "/register", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> register (@RequestBody String data) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("User");
        Document user = new Document().append("username", data).append("password", data).append("role", "USER");
        collection.insertOne(user);
        return ResponseEntity.ok("User registered");
    }

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
            if (role.equals("ADMIN")) {
                return ResponseEntity.ok("Admin logged in");
            } else if (role.equals("USER")) {
                return ResponseEntity.ok("User logged in");
            }
        }
        return ResponseEntity.ok("User not found");
    }



  /*  @PostMapping (value = "/logout", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity <String> logout (@RequestBody String data) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("User");
        Document user = new Document().append("username", data).append("password", data);
        FindIterable<Document> iterable = collection.find(user);
        Iterator<Document> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            Document document = (Document) iterator.next();
            String passwordFromDatabase = document.getString("password");
            if (passwordFromDatabase.equals(data)) {
                String token = new Util().generateToken(data, data);
                Token.getInstance().deleteToken(data);
                String role = document.getString("role");
                if (role.equals("ADMIN")) {
                    return ResponseEntity.ok("Admin logged out");
                } else if (role.equals("USER")) {
                    return ResponseEntity.ok("User logged out");
                }
            }
        }
        return ResponseEntity.ok("User not found");
    }


   */

    @PostMapping (value = "/logout", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity <String> logout (@RequestBody String


}

