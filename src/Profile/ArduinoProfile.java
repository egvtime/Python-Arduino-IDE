package Profile;

import java.util.*;
import javafx.scene.image.*;

public class ArduinoProfile {
    private int id;
    private String userName;
    private String password;
    private Vector<Project> projects;
    private Image ProfilePicture;

    public ArduinoProfile(int id, String userName, String password, Vector<Project> projects, Image profilePicture) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.projects = projects;
        ProfilePicture = profilePicture;
    }

    public ArduinoProfile(int id, String userName, String password, Vector<Project> projects) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.projects = projects;
        ProfilePicture = null;
    }

    public ArduinoProfile(String userName, String password, Vector<Project> projects, Image profilePicture) {
        this.userName = userName;
        this.password = password;
        this.projects = projects;
        ProfilePicture = profilePicture;
    }

    public ArduinoProfile(String userName, String password, Vector<Project> projects) {
        this.userName = userName;
        this.password = password;
        this.projects = projects;
        ProfilePicture = null;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public boolean setPassword(String password, String previousPassword) {
        if(previousPassword.equals(this.password)) {
            this.password = password;
            return true;
        }
        return false;
    }

    public Vector<Project> getProjects() {
        return projects;
    }

    public void setProjects(Vector<Project> projects) {
        this.projects = projects;
    }

    public Image getProfilePicture() {
        return ProfilePicture;
    }

    public void setProfilePicture(Image profilePicture) {
        ProfilePicture = profilePicture;
    }

    @Override
    public String toString() {
        return userName + " | #" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ArduinoProfile that)) return false;
        return id == that.id;
    }

    public static class Project{
        private String title;
        private String info;
        private String description;

        public Project(String title, String info, String description) {
            this.title = title;
            this.info = info;
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Project that)) return false;
            return this.info.equals(that.getInfo());
        }

        @Override
        public String toString() {
            return title + ":\n" + info;
        }
    }
}
