package sk.kasv.fekete.Database;

import com.mongodb.client.*;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.bson.Document;
import org.joda.time.format.DateTimeFormatter;
import sk.kasv.fekete.Controller.CoursesController;
import sk.kasv.fekete.Util.Role;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

/**
 * @author: Roland Fekete
 * @date: 2023.04.21
 * @description: This class is used to connect to the database
 */

public class DatabaseManager {
    private static CoursesController instance;
    private MongoCollection<Document> userCollection;
    private ServletRequest request;

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
    public static CoursesController getInstance() {
        return instance;
    }
    public static void setInstance(CoursesController instance) {
        DatabaseManager.instance = instance;
    }
    public void insertLogWithToken(Date date, String username) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("Lectures");
            MongoCollection<Document> collection = database.getCollection("Log");

            // Create the document to be inserted
            Document document = new Document()
                    .append("date", date)
                    .append("username", username);

            // Insert the document into the collection
            collection.insertOne(document);
        } catch (Exception e) {
            // Handle exceptions appropriately
        }
    }

    public void handleRequest(HttpServletRequest request, String username, DateTimeFormatter date) {
        // Call the insertLogWithToken method and pass the HttpServletRequest object
        //insertLogWithToken(request, username, date);
    }

    public void deleteTokenFromLog() {
      /*  MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("Log");
        collection.deleteMany(new Document());
       */
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

    public void deleteUser(String userToDelete) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("User");
        collection.deleteOne(eq("username", userToDelete));
    }

    public void changePassword(String username, String password, String newPassword) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("User");
        Document user = new Document().append("username", username).append("password", password);
        FindIterable<Document> iterable = collection.find(user);
        Iterator<Document> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            Document document = (Document) iterator.next();
            document.put("password", newPassword);
            collection.replaceOne(eq("username", username), document);
        }
    }
    public void reorderSeats() {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("Lectures");
            MongoCollection<Document> collection = database.getCollection("Course");

            // Retrieve all documents from the collection
            FindIterable<Document> documents = collection.find();

            for (Document document : documents) {
                // Retrieve the participants array from the document
                List<Document> participants = document.getList("participants", Document.class);

                // Create a map to store existing seats
                Map<Integer, Document> seatMap = new HashMap<>();

                // Populate the seat map and reorder seats
                for (Document participant : participants) {
                    Integer seat = participant.getInteger("seat");
                    if (seat != null) {
                        seatMap.put(seat, participant);
                    }
                }

                // Create a new list to store participants with reordered seats
                List<Document> reorderedParticipants = new ArrayList<>();

                // Add missing seats with empty participant entries
                for (int i = 1; i <= 30; i++) {
                    if (!seatMap.containsKey(i)) {
                        Document emptyParticipant = new Document("seat", i);
                        reorderedParticipants.add(emptyParticipant);
                    } else {
                        reorderedParticipants.add(seatMap.get(i));
                    }
                }

                // Update the participants list with reordered seats
                document.put("participants", reorderedParticipants);

                // Update the document in the collection
                collection.replaceOne(eq("_id", document.get("_id")), document);
            }
        } catch (Exception e) {
            // Handle exceptions appropriately
        }
}



}


