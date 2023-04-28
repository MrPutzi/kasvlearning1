package sk.kasv.fekete.Controller;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.kasv.fekete.Util.Lecture;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin
public class CoursesController extends RestController {

    @PostMapping(value = "/course/new", consumes = "application/json", produces = "application/json")
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

    @CrossOrigin
    @GetMapping(value = "/courses", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<List<Lecture>> getAllLectures() {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("Lectures");
        MongoCollection<Document> collection = database.getCollection("Course");
        List<Lecture> lectures = new ArrayList<>();
        for (Document doc : collection.find()) {
            Lecture lecture = new Lecture(
                    doc.getString("title"),
                    doc.getString("description"),
                    doc.getString("lector"),
                    doc.getDate("date") + " ",
                    doc.getList("participants", String.class)
            );
            lectures.add(lecture);
        }
        return ResponseEntity.ok(lectures);
    }
}