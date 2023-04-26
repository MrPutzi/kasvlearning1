package sk.kasv.fekete;

import sk.kasv.fekete.Database.DatabaseManager;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import static org.springframework.boot.SpringApplication.*;

@SpringBootApplication
@EnableMongoRepositories
public class Main {
    public static void main(String[] args) {
        run(Main.class, args);




        }
    }
