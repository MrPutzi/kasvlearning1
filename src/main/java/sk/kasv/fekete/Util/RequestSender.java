package sk.kasv.fekete.Util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class RequestSender {
    private final String apiKey = "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";

    public void sendRequestAndWriteToMongoDB(String title, String description, String lector, String date) {
        try {
            // Create a connection to the API endpoint
           URL apiUrl = new URL ("https://api.openai.com/v1/chat/completions");

            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);

            // Create the JSON payload using the template and provided data
            String jsonPayload = "{\n" +
                    "  \"title\": \"" + title + "\",\n" +
                    "  \"description\": \"" + description + "\",\n" +
                    "  \"lector\": \"" + lector + "\",\n" +
                    "  \"date\": \"" + date + "\",\n" +
                    "  \"participants\": [\n" +
                    "    {\n" +
                    "      \"seat\": 1\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 2\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 3\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 4\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 5\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 6\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 7\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 8\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 9\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 10\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 11\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 12\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 13\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 14\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 15\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 16\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 17\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 18\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 19\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 20\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 21\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 22\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 23\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 24\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 25\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 26\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 27\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 28\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 29\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"seat\": 30\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            // Write the JSON payload to the request body
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonPayload.getBytes());
            outputStream.flush();
            outputStream.close();

            // Get the response from the API
            int responseCode = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Write the response to MongoDB
            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase database = mongoClient.getDatabase("Lectures");
            MongoCollection<org.bson.Document> collection = database.getCollection("Course");
            Document document = new Document("title", title)
                    .append("description", description)
                    .append("lector", lector)
                    .append("date", date)
                    .append("responseCode", responseCode)
                    .append("response", response.toString());
            collection.insertOne(document);

            // Close the MongoDB connection
            mongoClient.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        RequestSender sender = new RequestSender();

        // Example usage
        String title = "Introduction to Java";
        String description = "Learn the basics of Java programming language";
        String lector = "John Doe";
        String date = "2023-06-16";
        sender.sendRequestAndWriteToMongoDB(title, description, lector, date);
    }
}