package sk.kasv.fekete.Controller;

import com.google.gson.JsonObject;
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
  /* @PostMapping(value = "/course/new", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> addLecture(@RequestBody Lecture lecture) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("Course");
        Document document = new Document()
                .append("title", lecture.getTitle())
                .append("description", lecture.getDescription())
                .append("lector", lecture.getLector())
                .append("date", lecture.getDate())
                .append("participants", lecture.getParticipants());
        collection.insertOne(document);
        if (lecture.getTitle().equals("") || lecture.getDescription().equals("") || lecture.getLector().equals("") || lecture.getDate().equals("") || lecture.getParticipants().equals("")) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }

   */
    /*
    @CrossOrigin
    @GetMapping(value = "/courses" , consumes = "application/json", produces = "application/json" )
    @ResponseBody
    public ResponseEntity<List<Lecture>> getAllCourses(@RequestHeader String token) {
        if (token.contains("Bearer")) {
        token = token.substring(7);}
        DatabaseManager db = new DatabaseManager();
        String username = tokens.get(token);
                if (token == null || !tokens.containsKey(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        else {
            try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
                MongoDatabase database = mongoClient.getDatabase("Lectures");
                MongoCollection<Document> collection = database.getCollection("Course");
                FindIterable<Document> documents = collection.find();
                for (Document document : documents) {
                    List<Lecture> lectures = new ArrayList<>();
                    Lecture lecture = new Lecture();
                    lecture.setTitle(document.getString("title"));
                    lecture.setDescription(document.getString("description"));
                    lecture.setLector(document.getString("lector"));
                    lecture.setDate(document.getString("date"));
                    lecture.setParticipants(Collections.singletonList(document.getString("participants")));
                    lectures.add(lecture);
                    return ResponseEntity.ok(lectures);
                }
            }
        } return new ResponseEntity<>(null, HttpStatus.OK);
}

@CrossOrigin
    @PutMapping (value = "/course/{id}")
    @ResponseBody
    public ResponseEntity updateCourse(@PathVariable String id, @RequestBody Lecture lecture) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("Lectures");
            MongoCollection<Document> collection = database.getCollection("Course");
            Document document = new Document()
                    .append("title", lecture.getTitle())
                    .append("description", lecture.getDescription())
                    .append("lector", lecture.getLector())
                    .append("date", lecture.getDate())
                    .append("participants", lecture.getParticipants());
            collection.updateOne(new Document("_id", id), new Document("$set", document));
            return ResponseEntity.ok("Course updated");
        } catch (MongoException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
*/

    @GetMapping("/courses")
    public ResponseEntity<Object> getCourses(@RequestHeader String token) {
        if (token.contains("Bearer")) {
            token = token.substring(7);
        }
        if (!tokens.containsKey(token)) {
            ResponseEntity.badRequest().body(new JSONObject(Map.of("Error:", "Invalid token.")));
        }
        List<JSONObject> courses = new ArrayList<>();
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("Course");
        FindIterable<Document> iterable = collection.find();
        for (Document document : iterable) {
            JSONObject course = new JSONObject();
            course.put("courseName", document.getString("title"));
            course.put("courseDescription", document.getString("description"));
            course.put("courseStartDate", document.getString("date"));
            course.put("courseAttendances", document.getString("participants"));
            courses.add(course);
        }
        return ResponseEntity.ok(courses);
    }
    @GetMapping("/mycourses")
    @ResponseBody
    public ResponseEntity<Object> getUserCourses(@RequestHeader("token") String token) {
        JsonObject response = new JsonObject();
        if (token.contains("Bearer")) {
            token = token.substring(7);
        }
        if (!tokens.containsKey(token)) {
            ResponseEntity.badRequest().body(new JSONObject(Map.of("Error:", "Invalid token.")));
        }
        String username = tokens.get(token);
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("Course");
        Document query = new Document("participants", username);
        FindIterable<Document> iterable = collection.find(query);
        List<JSONObject> courses = new ArrayList<>();
        for (Document document : iterable) {
            JSONObject course = new JSONObject();
            course.put("courseName", document.getString("title"));
            course.put("courseDescription", document.getString("description"));
            course.put("courseStartDate", document.getString("date"));
            courses.add(course);
            return ResponseEntity.ok(courses);
        }
        return ResponseEntity.ok(courses);
    }
    @PutMapping("/course/{id}/reserve")
    public ResponseEntity<Object> reserveSeat(@PathVariable String id, @RequestHeader String token) {
        DatabaseManager db = new DatabaseManager();
        String username = tokens.get(token);
        Token.getInstance().validateToken(username, token);
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("Course");
        Document query = new Document("_id", new ObjectId(id));
        FindIterable<Document> iterable = collection.find(query);
        Iterator<Document> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            Document document = iterator.next();
            List<String> participants = (List<String>) document.get("participants");
            if (participants.contains(username)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already reserved a seat");
            } else if (participants.size() == Integer.parseInt(document.getString("seats"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Course is full");
            } else {
                participants.add(username);
                collection.updateOne(query, new Document("$set", new Document("participants", participants)));
                return ResponseEntity.status(HttpStatus.CREATED).body(new JSONObject().appendField("message", "Seat reserved"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Course not found");
        }
    }

}