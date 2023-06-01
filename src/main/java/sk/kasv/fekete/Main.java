package sk.kasv.fekete;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.CrossOrigin;
import sk.kasv.fekete.Database.DatabaseManager;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import sk.kasv.fekete.Util.CorsFilter;
import sk.kasv.fekete.Util.Token;

import static org.springframework.boot.SpringApplication.*;

@SpringBootApplication
@EnableMongoRepositories
@CrossOrigin
public class Main {
    private DatabaseManager databaseManager;

    public static void main(String[] args) {
        run(Main.class, args);
        }
        //swagger html link : http://localhost:8080/swagger-ui.html

    @Bean
    public CorsFilter corsFilter() {
        CorsFilter filter = new CorsFilter();
        return filter;
    }
    /**
     * @name: DESTROY
     * @description: This method is used to clear out the log collection in the database when the server is shut down.
     * @author: Roland Fekete
     * @date: 18/05/2023
     **/

    @PreDestroy
    public void destroy() {
        databaseManager.deleteTokenFromLog();

    }

    }
