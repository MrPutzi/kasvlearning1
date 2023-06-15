package sk.kasv.fekete.Util;

import java.util.Date;
import java.util.List;

public class CourseRequest {
        private String date;
        private String lector;
        private String description;
        private String id;
        private String title;
        private List<Participant> participants;

    public CourseRequest(String date, String lector, String description, String id, String title, List<Participant> participants) {
        this.date = date;
        this.lector = lector;
        this.description = description;
        this.id = id;
        this.title = title;
        this.participants = participants;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLector() {
        return lector;
    }

    public void setLector(String lector) {
        this.lector = lector;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public Date getDateAsDate() {
        return new Date(Long.parseLong(date));

    }

// Add getters and setters
        // ...



        public static class Participant {

            private int seat;
            private String username;

            public Participant(int seat, String username) {
                this.seat = seat;
                this.username = username;
            }

            public int getSeat() {
                return seat;
            }

            public void setSeat(int seat) {
                this.seat = seat;
            }

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }
        }
    }


