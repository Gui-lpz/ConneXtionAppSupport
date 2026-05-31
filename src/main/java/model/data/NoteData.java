package model.data;

import java.sql.*;
import java.util.ArrayList;
import model.entities.Note;

public class NoteData {

    private Note map(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("note_timestamp");

        return new Note(
                rs.getInt("id"),
                rs.getString("description"),
                timestamp != null ? timestamp.toLocalDateTime() : null,
                rs.getInt("issue_id"),
                rs.getInt("author_id"),
                rs.getString("author_type")
        );
    }

    public void add(Note note) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO Note "
                + "(description, note_timestamp, issue_id, author_id, author_type) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, note.getDescription());
            stmt.setTimestamp(2, Timestamp.valueOf(note.getNoteTimestamp()));
            stmt.setInt(3, note.getIssueId());
            stmt.setInt(4, note.getAuthorId());
            stmt.setString(5, note.getAuthorType());

            stmt.executeUpdate();
        }
    }

    public ArrayList<Note> getByIssueId(int issueId) throws SQLException, ClassNotFoundException {
        ArrayList<Note> list = new ArrayList<>();
        String sql = "SELECT * FROM Note WHERE issue_id=? ORDER BY note_timestamp ASC";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, issueId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }

        return list;
    }

    public Note findById(int id) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM Note WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }

        return null;
    }

    public void delete(int id) throws SQLException, ClassNotFoundException {
        String sql = "DELETE FROM Note WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}