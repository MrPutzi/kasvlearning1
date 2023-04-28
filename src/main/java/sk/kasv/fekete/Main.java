package sk.kasv.fekete;

import org.springframework.web.bind.annotation.CrossOrigin;
import sk.kasv.fekete.Database.DatabaseManager;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import static org.springframework.boot.SpringApplication.*;

@SpringBootApplication
@EnableMongoRepositories
@CrossOrigin
public class Main {
    public static void main(String[] args) {
        run(Main.class, args);





        }
    }
