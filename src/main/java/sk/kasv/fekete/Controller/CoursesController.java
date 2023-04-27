package sk.kasv.fekete.Controller;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import sk.kasv.fekete.Util.Lecture;

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

    @GetMapping(value = "/courses", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> getLectures(@Re) {


    }
}