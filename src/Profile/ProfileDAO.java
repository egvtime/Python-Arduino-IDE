package Profile;

import Profile.ArduinoProfile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Vector;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

public class ProfileDAO {
    private static final String URL      = "jdbc:postgresql://localhost:5432/ArduinoProfile";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "mizuhara";

    public int getHighestID() throws SQLException {
        String sql = "SELECT COALESCE(MAX(id),0) AS max_id FROM profiles";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("max_id");
            }
        }
        return 0;
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM profiles WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean saveProfile(ArduinoProfile profile) throws SQLException, IOException {
        if (usernameExists(profile.getUserName())) {
            return false;
        }

        String sql = "INSERT INTO profiles("
                + "id, username, password, picture, project_titles, project_infos, project_descriptions"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, profile.getId());
            ps.setString(2, profile.getUserName());
            ps.setString(3, profile.getPassword());

            byte[] picBytes = null;
            Image pic = profile.getProfilePicture();
            if (pic != null) {
                int w = (int) pic.getWidth();
                int h = (int) pic.getHeight();
                WritableImage wr = new WritableImage(w, h);
                new ImageView(pic).snapshot(new SnapshotParameters(), wr);
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    ImageIO.write(SwingFXUtils.fromFXImage(wr, null), "png", baos);
                    picBytes = baos.toByteArray();
                }
            }
            ps.setBytes(4, picBytes);

            Vector<ArduinoProfile.Project> projects = profile.getProjects();
            int n = projects.size();
            String[] titles = new String[n];
            String[] infos  = new String[n];
            String[] descs  = new String[n];
            for (int i = 0; i < n; i++) {
                ArduinoProfile.Project p = projects.get(i);
                titles[i] = p.getTitle();
                infos[i]  = p.getInfo();
                descs[i]  = p.getDescription();
            }
            ps.setArray(5, conn.createArrayOf("text", titles));
            ps.setArray(6, conn.createArrayOf("text", infos));
            ps.setArray(7, conn.createArrayOf("text", descs));

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateProfile(ArduinoProfile profile) throws SQLException, IOException {
        String existingUsername = getUsernameById(profile.getId());
        String newUsername      = profile.getUserName();

        if (!newUsername.equals(existingUsername) && usernameExists(newUsername)) {
            return false;
        }

        String sql = "UPDATE profiles SET "
                + "username = ?, password = ?, picture = ?, "
                + "project_titles = ?, project_infos = ?, project_descriptions = ? "
                + "WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, profile.getUserName());
            ps.setString(2, profile.getPassword());

            byte[] picBytes = null;
            Image pic = profile.getProfilePicture();
            if (pic != null) {
                int w = (int) pic.getWidth();
                int h = (int) pic.getHeight();
                WritableImage wr = new WritableImage(w, h);
                new ImageView(pic).snapshot(new SnapshotParameters(), wr);
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    ImageIO.write(SwingFXUtils.fromFXImage(wr, null), "png", baos);
                    picBytes = baos.toByteArray();
                }
            }
            ps.setBytes(3, picBytes);

            Vector<ArduinoProfile.Project> projects = profile.getProjects();
            int n = projects.size();
            String[] titles = new String[n];
            String[] infos  = new String[n];
            String[] descs  = new String[n];
            for (int i = 0; i < n; i++) {
                ArduinoProfile.Project p = projects.get(i);
                titles[i] = p.getTitle();
                infos[i]  = p.getInfo();
                descs[i]  = p.getDescription();
            }
            ps.setArray(4, conn.createArrayOf("text", titles));
            ps.setArray(5, conn.createArrayOf("text", infos));
            ps.setArray(6, conn.createArrayOf("text", descs));
            ps.setInt(7, profile.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public ArduinoProfile getProfileThroughUsername(String username) throws SQLException, IOException {
        String sql = "SELECT id, password, picture, project_titles, project_infos, project_descriptions "
                + "FROM profiles WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id          = rs.getInt("id");
                    String pwd      = rs.getString("password");
                    byte[] picBytes = rs.getBytes("picture");
                    Image pic       = null;
                    if (picBytes != null && picBytes.length > 0) {
                        pic = new Image(new ByteArrayInputStream(picBytes));
                    }

                    Array arrTitles = rs.getArray("project_titles");
                    Array arrInfos  = rs.getArray("project_infos");
                    Array arrDescs  = rs.getArray("project_descriptions");

                    String[] titles = (String[]) arrTitles.getArray();
                    String[] infos  = (String[]) arrInfos.getArray();
                    String[] descs  = (String[]) arrDescs.getArray();

                    Vector<ArduinoProfile.Project> projects = new Vector<>();
                    for (int i = 0; i < titles.length; i++) {
                        projects.add(new ArduinoProfile.Project(
                                titles[i], infos[i], descs[i]
                        ));
                    }

                    return new ArduinoProfile(id, username, pwd, projects, pic);
                }
            }
        }
        return null;
    }

    public ArduinoProfile getProfileThroughID(int id) throws SQLException, IOException {
        String sql = "SELECT username, password, picture, project_titles, project_infos, project_descriptions "
                + "FROM profiles WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("username");
                    String password = rs.getString("password");

                    byte[] picBytes = rs.getBytes("picture");
                    Image pic = null;
                    if (picBytes != null && picBytes.length > 0) {
                        pic = new Image(new ByteArrayInputStream(picBytes));
                    }

                    Array arrTitles = rs.getArray("project_titles");
                    Array arrInfos  = rs.getArray("project_infos");
                    Array arrDescs  = rs.getArray("project_descriptions");

                    String[] titles = (String[]) arrTitles.getArray();
                    String[] infos  = (String[]) arrInfos.getArray();
                    String[] descs  = (String[]) arrDescs.getArray();

                    Vector<ArduinoProfile.Project> projects = new Vector<>();
                    for (int i = 0; i < titles.length; i++) {
                        projects.add(new ArduinoProfile.Project(
                                titles[i], infos[i], descs[i]
                        ));
                    }
                    return new ArduinoProfile(id, username, password, projects, pic);
                }
            }
        }
        return null;
    }

    public boolean deleteProfile(int id) throws SQLException {
        String sql = "DELETE FROM profiles WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean removeProject(int profileId, String projectTitle) throws SQLException, IOException {
        ArduinoProfile profile = getProfileThroughID(profileId);
        if (profile == null) return false;

        Vector<ArduinoProfile.Project> projects = profile.getProjects();
        boolean removed = projects.removeIf(p -> p.getTitle().equals(projectTitle));
        if (!removed) return false;

        return updateProfile(profile);
    }

    private String getUsernameById(int id) throws SQLException {
        String sql = "SELECT username FROM profiles WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        }
        return null;
    }
}
