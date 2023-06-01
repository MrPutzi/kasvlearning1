package sk.kasv.fekete.Util;

import java.util.ArrayList;
import java.util.List;

public class Lecture {
    private String title;
    private String description;
    private String lector;
    private String date;
    private List<String> participants;
    private List<Integer> seats;

    public Lecture() {
        // Default constructor
    }

    public Lecture(String title, String description, String lector, String date) {
        this.title = title;
        this.description = description;
        this.lector = lector;
        this.date = date;
        this.seats = generateSeats(30); // Generate 30 seats for each lecture
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLector() {
        return lector;
    }

    public void setLector(String lector) {
        this.lector = lector;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public List<Integer> getSeats() {
        return seats;
    }

    public void setSeats(List<Integer> seats) {
        this.seats = seats;
    }

    private List<Integer> generateSeats(int seatCount) {
        // Generate a list of seats with numbers from 1 to seatCount
        List<Integer> seats = new ArrayList<>();
        for (int i = 1; i <= seatCount; i++) {
            seats.add(i);
        }
        return seats;
    }
}
