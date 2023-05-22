package sk.kasv.fekete.Util;

import java.util.List;

public class Lecture {
    private String title;
    private String description;
    private String lector;
    private String date;
    private List <String> participants;

    public Lecture() {
        this.title = title;
        this.description = description;
        this.lector = lector;
        this.date = String.valueOf(date);
        this.participants = participants;
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
}
