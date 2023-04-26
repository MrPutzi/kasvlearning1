package sk.kasv.fekete.Database;

import com.mongodb.client.*;
import org.bson.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import sk.kasv.fekete.Util.Role;

import java.util.Iterator;

import static com.mongodb.client.model.Filters.eq;

/**
 * @author: Roland Fekete
 * @date: 2023.04.21
 * @description: This class is used to connect to the database
 */
public class getConnection {
    private MongoCollection<Document> userCollection;
    /**
     * @param username
     * @param password
     * @return if user exists and password is correct, return role constant
     * if user does not exist or password is incorrect, return
     * @description: This method checks if the user exists in the database and if the password is correct
     * @date: 2023.04.21
     */

    public Role checkUser(String username, String password) {
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






}

