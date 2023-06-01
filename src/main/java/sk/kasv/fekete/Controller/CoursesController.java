package sk.kasv.fekete.Controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import net.minidev.json.JSONObject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.kasv.fekete.Database.DatabaseManager;
import sk.kasv.fekete.Util.Lecture;
import sk.kasv.fekete.Util.Token;
import sk.kasv.fekete.Util.User;

import java.util.*;

@org.springframework.web.bind.annotation.RestController
@CrossOrigin
public class CoursesController {

    Map<String, String> myCourses = new HashMap<>();
    Map<String, String> tokens = new HashMap<>();

    /**
     * @name: ADD COURSE
     * @description: This method is used to add a course to the database
     * @author: Roland Fekete
     * @date: 18/05/2023
     **/

    @GetMapping("/courses")
    public ResponseEntity<Object> getCourses(@RequestHeader String token) {
        if (token.contains("Bearer")) {
            token = token.substring(7);
        }
        String username = Token.getInstance().validateUsername(token);

        if (username == null) {
            JsonObject response = new JsonObject();
            response.addProperty("error", "Invalid token.");
            return ResponseEntity.badRequest().body(response.toString());
        }

        try {
            List<JSONObject> courses = new ArrayList<>();
            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase database = mongoClient.getDatabase("Lectures");
            MongoCollection<Document> collection = database.getCollection("Course");
            FindIterable<Document> iterable = collection.find();
            for (Document document : iterable) {
                JSONObject course = new JSONObject();
                course.put("courseId", document.getObjectId("_id").toString());
                course.put("courseTitle", document.getString("title"));
                course.put("courseDescription", document.getString("description"));
                course.put("courseStartDate", document.getString("date"));

                List<JSONObject> participants = new ArrayList<>();
                List<Document> participantDocuments = document.getList("participants", Document.class);
                for (Document participantDocument : participantDocuments) {
                    JSONObject participant = new JSONObject();
                    if (participantDocument.containsKey("name")) {
                        participant.put("name", participantDocument.getString("name"));
                    }
                    participant.put("seat", participantDocument.getInteger("seat"));
                    participants.add(participant);
                }

                course.put("courseParticipants", participants);
                courses.add(course);
            }
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("error", "Error retrieving courses.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response.toString());
        }
    }

    @GetMapping("/mycourses")
    public ResponseEntity<Object> getUserCourses(@RequestHeader("token") String token) {
        if (token.contains("Bearer")) {
            token = token.substring(7);
        }
        String username = Token.getInstance().validateUsername(token); // Retrieve username based on the token
        if (username == null) {
            JsonObject response = new JsonObject();
            response.addProperty("error", "Invalid token.");
            return ResponseEntity.badRequest().body(response.toString());
        }
        try {
            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase database = mongoClient.getDatabase("Lectures");
            MongoCollection<Document> collection = database.getCollection("Course");
            Document query = new Document("participants", new Document("$elemMatch", new Document("name", username)));
            FindIterable<Document> iterable = collection.find(query);
            List<JSONObject> courses = new ArrayList<>();
            for (Document document : iterable) {
                JSONObject course = new JSONObject();
                course.put("courseName", document.getString("title"));
                course.put("courseDescription", document.getString("description"));
                course.put("courseStartDate", document.getString("date"));

                List<JSONObject> participants = new ArrayList<>();
                List<Document> participantDocuments = document.getList("participants", Document.class);
                for (Document participantDocument : participantDocuments) {
                    if (participantDocument.containsKey("name") && participantDocument.getString("name").equals(username)) {
                        JSONObject participant = new JSONObject();
                        participant.put("name", participantDocument.getString("name"));
                        participant.put("seat", participantDocument.getInteger("seat"));
                        participants.add(participant);
                        break;
                    }
                }

                course.put("courseAttendances", participants);
                courses.add(course);
            }
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("error", "Error retrieving user courses.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response.toString());
        }
    }

    @PutMapping("/course/{id}/reserve")
    @ResponseBody
    public ResponseEntity<String> reserveSeat(@PathVariable String id, @RequestBody String data, @RequestHeader("token") String token) {
        try {
            if (token.contains("Bearer")) {
                token = token.substring(7);
            }
            String username = Token.getInstance().validateUsername(token); // Retrieve username based on the token
            if (username == null) {
                JsonObject response = new JsonObject();
                response.addProperty("error", "Invalid token.");
                return ResponseEntity.badRequest().body(response.toString());
            }
            // Parse the request body to get the seat number
            JsonObject requestData = new JsonParser().parse(data).getAsJsonObject();
            int seatNumber = requestData.get("seat").getAsInt();
            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase database = mongoClient.getDatabase("Lectures");
            MongoCollection<Document> collection = database.getCollection("Course");
            // Build the query to find the course
            ObjectId courseId = new ObjectId(id);
            Document query = new Document("_id", courseId);
            // Find the course
            FindIterable<Document> iterable = collection.find(query);
            Document courseDocument = iterable.first();
            if (courseDocument == null) {
                JsonObject response = new JsonObject();
                response.addProperty("error", "Course not found.");
                return ResponseEntity.badRequest().body(response.toString());
            }
            // Retrieve the participants array
            List<Document> participants = (List<Document>) courseDocument.get("participants");
            // Check if the seat number is already taken
          /*
          for (Document participant : participants) {
                int participantSeat = participant.getInteger("seat");
                if (participantSeat == seatNumber) {
                    JsonObject response = new JsonObject();
                    response.addProperty("error", "Seat is already taken.");
                    return ResponseEntity.badRequest().body(response.toString());
                }
            }
            */
            for (Document participant : participants) {
                String participantName = participant.getString("name");
                if (participantName != null && participantName.equals(username)) {
                    JsonObject response = new JsonObject();
                    response.addProperty("error", "Seat is already taken.");
                    return ResponseEntity.badRequest().body(response.toString());
                }
            }
            // Update the participants array to reserve the seat for the user
            Document participantDocument = new Document("name", username).append("seat", seatNumber);
            participants.add(participantDocument);
            courseDocument.put("participants", participants);
            // Update the course in the database
            collection.replaceOne(query, courseDocument);
            JsonObject response = new JsonObject();
            response.addProperty("message", "Seat reserved successfully.");
            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("error", "Error reserving seat.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response.toString());
        }
    }

    @PutMapping("/course/{id}/unreserve")
    @ResponseBody
    public ResponseEntity<String> unassignSeat(@PathVariable String id, @RequestBody String data, @RequestHeader("token") String token) {
        try {
            if (token.contains("Bearer")) {
                token = token.substring(7);
            }
            String username = Token.getInstance().validateUsername(token); // Retrieve username based on the token
            if (username == null) {
                JsonObject response = new JsonObject();
                response.addProperty("error", "Invalid token.");
                return ResponseEntity.badRequest().body(response.toString());
            }
            // Parse the request body to get the seat number
            JsonObject requestData = new JsonParser().parse(data).getAsJsonObject();
            int seatNumber = requestData.get("seat").getAsInt();
            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase database = mongoClient.getDatabase("Lectures");
            MongoCollection<Document> collection = database.getCollection("Course");
            // Build the query to find the course
            ObjectId courseId = new ObjectId(id);
            Document query = new Document("_id", courseId);
            // Find the course
            FindIterable<Document> iterable = collection.find(query);
            Document courseDocument = iterable.first();
            if (courseDocument == null) {
                JsonObject response = new JsonObject();
                response.addProperty("error", "Course not found.");
                return ResponseEntity.badRequest().body(response.toString());
            }
            // Retrieve the participants array
            List<Document> participants = (List<Document>) courseDocument.get("participants");
            // Find and remove the participant with the specified seat number
            boolean participantFound = false;
            for (Iterator<Document> iterator = participants.iterator(); iterator.hasNext(); ) {
                Document participant = iterator.next();
              /*
              int participantSeat = participant.getInteger("seat", -1);
                String participantName = participant.getString("name");
                if (participantSeat == seatNumber && (participantName == null || participantName.equals(username))) {
                    iterator.remove();
                    participantFound = true;
                    break;
                }
                */
                String participantName = participant.getString("name");
                if (participantName != null && participantName.equals(username)) {
                    iterator.remove();
                    participantFound = true;
                    break;
                }
            }
            if (!participantFound) {
                JsonObject response = new JsonObject();
                response.addProperty("error", "Participant not found or not assigned to the seat.");
                return ResponseEntity.badRequest().body(response.toString());
            }
            // Update the course in the database with the modified participants array
            courseDocument.put("participants", participants);
            collection.replaceOne(query, courseDocument);
            JsonObject response = new JsonObject();
            response.addProperty("message", "Seat unassigned successfully.");
            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("error", "Error unassigning seat.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response.toString());
        }
    }




}