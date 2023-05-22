package sk.kasv.fekete.Database;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.*;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.bson.Document;
import sk.kasv.fekete.Util.Role;
import sk.kasv.fekete.Util.Token;
import sk.kasv.fekete.Controller.RestController;

import java.util.Date;
import java.util.Iterator;

import static com.mongodb.client.model.Filters.eq;

/**
 * @author: Roland Fekete
 * @date: 2023.04.21
 * @description: This class is used to connect to the database
 */

public class DatabaseManager {
    private MongoCollection<Document> userCollection;

    /**
     * @param username
     * @param password
     * @description: This method checks if the user exists in the database and if the password is correct
     * @date: 2023.04.21
     */

    public static Role checkUser(String username, String password) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("User");
       Document user = new Document().append("username", username).append("password", password);
        FindIterable<Document> iterable = collection.find(user);
        Iterator<Document> iterator = iterable.iterator();
          if (iterator.hasNext()) {
        Document document = (Document) iterator.next();
            String passwordFromDatabase = document.getString("password");
            if (passwordFromDatabase.equals(password)) {
                String role = document.getString("role");
                if (role.equals("ADMIN")) {
                    return Role.ADMIN;
                } else if (role.equals("USER")) {
                    return Role.USER;
                }
            }
        }
        return null;
    }

    //insert log in database
    public void insertLogWithToken(String username, Date date, String token) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("Log");
        Document document = new Document()
                .append("username", username)
                .append("date", date)
                .append("token", token);
        collection.insertOne(document);
    }

    //check the log if the user is not logged in
    public String checkLog(String username) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("Log");
        Document user = new Document().append("username", username);
        FindIterable<Document> iterable = collection.find(user);
        Iterator<Document> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            Document document = (Document) iterator.next();
            String token = document.getString("token");
            return token;
        } else {
            return null;
        }
    }

    //clears the whole log collection
    public void deleteTokenFromLog() {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("Log");
        collection.deleteMany(new Document());
    }


    public String getUsernameFromToken(String token) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("Log");
        Document user = new Document().append("token", token);
        FindIterable<Document> iterable = collection.find(user);
        Iterator<Document> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            Document document = (Document) iterator.next();
            String username = document.getString("username");
            return username;
        } else {
            return null;
        }
    }





}

