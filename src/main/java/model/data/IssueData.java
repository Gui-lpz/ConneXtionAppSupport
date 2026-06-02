package model.data;

import java.sql.*;
import java.util.ArrayList;
import model.entities.Issue;

public class IssueData {

    private Issue map(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("issue_timestamp");

        int supporterId = rs.getInt("supporter_id");
        if (rs.wasNull()) {
            supporterId = 0;
        }

        int supervisorId = rs.getInt("supervisor_id");
        if (rs.wasNull()) {
            supervisorId = 0;
        }

        return new Issue(
                rs.getInt("id"),
                rs.getString("reference"),
                rs.getString("classification"),
                rs.getString("status"),
                timestamp != null ? timestamp.toLocalDateTime() : null,
                rs.getString("resolution_comment"),
                rs.getInt("service_id"),
                supporterId,
                supervisorId
        );
    }

    public void add(Issue issue) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO Issue "
                + "(reference, classification, status, issue_timestamp, resolution_comment, "
                + "service_id, supporter_id, supervisor_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, issue.getReference());
            stmt.setString(2, issue.getClassification());
            stmt.setString(3, issue.getStatus());
            stmt.setTimestamp(4, Timestamp.valueOf(issue.getIssueTimestamp()));
            stmt.setString(5, issue.getResolutionComment());
            stmt.setInt(6, issue.getServiceId());

            if (issue.getSupporterId() == 0) {
                stmt.setNull(7, Types.INTEGER);
            } else {
                stmt.setInt(7, issue.getSupporterId());
            }

            if (issue.getSupervisorId() == 0) {
                stmt.setNull(8, Types.INTEGER);
            } else {
                stmt.setInt(8, issue.getSupervisorId());
            }

            stmt.executeUpdate();
        }
    }

    public ArrayList<Issue> getAll() throws SQLException, ClassNotFoundException {
        ArrayList<Issue> list = new ArrayList<>();
        String sql = "SELECT * FROM Issue ORDER BY issue_timestamp ASC";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }

        return list;
    }

    public Issue findById(int id) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM Issue WHERE id=?";

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

    public void assignSupporter(int issueId, int supporterId, int supervisorId) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Issue SET supporter_id=?, supervisor_id=?, status=? WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supporterId);
            stmt.setInt(2, supervisorId);
            stmt.setString(3, "Asignado");
            stmt.setInt(4, issueId);

            stmt.executeUpdate();
        }
    }

    public void startProcess(int issueId) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Issue SET status=? WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "En Progreso");
            stmt.setInt(2, issueId);

            stmt.executeUpdate();
        }
    }

    public void resolveIssue(int issueId, String resolutionComment) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Issue SET status=?, resolution_comment=? WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "Resuelto");
            stmt.setString(2, resolutionComment);
            stmt.setInt(3, issueId);

            stmt.executeUpdate();
        }
    }

    public void update(Issue issue) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Issue SET reference=?, classification=?, status=?, issue_timestamp=?, "
                + "resolution_comment=?, service_id=?, supporter_id=?, supervisor_id=? WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, issue.getReference());
            stmt.setString(2, issue.getClassification());
            stmt.setString(3, issue.getStatus());
            stmt.setTimestamp(4, Timestamp.valueOf(issue.getIssueTimestamp()));
            stmt.setString(5, issue.getResolutionComment());
            stmt.setInt(6, issue.getServiceId());

            if (issue.getSupporterId() == 0) {
                stmt.setNull(7, Types.INTEGER);
            } else {
                stmt.setInt(7, issue.getSupporterId());
            }

            if (issue.getSupervisorId() == 0) {
                stmt.setNull(8, Types.INTEGER);
            } else {
                stmt.setInt(8, issue.getSupervisorId());
            }

            stmt.setInt(9, issue.getId());

            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException, ClassNotFoundException {
        String sql = "DELETE FROM Issue WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}