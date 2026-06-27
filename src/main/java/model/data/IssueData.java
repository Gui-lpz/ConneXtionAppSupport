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
        String serviceName = null;
        try {
            serviceName = rs.getString("service_name");
        } catch (SQLException e) {
            // Si la consulta no incluyó simplemente  queda como null
        }
        Issue issue = new Issue(
                rs.getInt("id"),
                rs.getString("reference"),
                rs.getString("classification"),
                rs.getString("status"),
                timestamp != null ? timestamp.toLocalDateTime() : null,
                rs.getString("resolution_comment"),
                null,
                null,
                null,
                null,
                rs.getInt("service_id"),
                serviceName, // Agregado exclusivamente para la interfaz
                supporterId,
                supervisorId
        );
        issue.setIssueDescription(rs.getString("issue_description"));
        return issue;
    }

    public void add(Issue issue) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO Issue "
                + "(reference, classification, status, issue_timestamp, resolution_comment, "
                + "service_id, supporter_id, supervisor_id, issue_description) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection_AppSupport.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, issue.getReference());
            statement.setString(2, issue.getClassification());
            statement.setString(3, issue.getStatus());
            statement.setTimestamp(4, Timestamp.valueOf(issue.getIssueTimestamp()));
            statement.setString(5, issue.getResolutionComment());
            statement.setInt(6, issue.getServiceId());

            if (issue.getSupporterId() == 0) {
                statement.setNull(7, Types.INTEGER);
            } else {
                statement.setInt(7, issue.getSupporterId());
            }

            if (issue.getSupervisorId() == 0) {
                statement.setNull(8, Types.INTEGER);
            } else {
                statement.setInt(8, issue.getSupervisorId());
            }
            statement.setString(9, issue.getIssueDescription());
            statement.executeUpdate();
        }
    }

    public ArrayList<Issue> getAll() throws SQLException, ClassNotFoundException {
        ArrayList<Issue> list = new ArrayList<>();
        String sql = "SELECT i.*, s.name AS service_name "
                + "FROM Issue i "
                + "LEFT JOIN Service s ON i.service_id = s.id "
                + "ORDER BY i.issue_timestamp ASC";
        try (Connection conn = DbConnection_AppSupport.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }

        return list;
    }

    public Issue findById(int id) throws SQLException, ClassNotFoundException {
        //String sql = "SELECT * FROM Issue WHERE id=?";
        String sql = "SELECT i.*, s.name AS service_name "
                + "FROM Issue i "
                + "LEFT JOIN Service s ON i.service_id = s.id "
                + "WHERE i.id=?";
        try (Connection conn = DbConnection_AppSupport.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

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

        try (Connection conn = DbConnection_AppSupport.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supporterId);
            stmt.setInt(2, supervisorId);
            stmt.setString(3, "Asignado");
            stmt.setInt(4, issueId);

            stmt.executeUpdate();
        }
    }

    public void startProcess(int issueId) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Issue SET status=? WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "En Progreso");
            stmt.setInt(2, issueId);

            stmt.executeUpdate();
        }
    }

    public void resolveIssue(int issueId, String resolutionComment) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Issue SET status=?, resolution_comment=? WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "Resuelto");
            stmt.setString(2, resolutionComment);
            stmt.setInt(3, issueId);

            stmt.executeUpdate();
        }
    }

    public void update(Issue issue) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Issue SET reference=?, classification=?, status=?, issue_timestamp=?, "
                + "resolution_comment=?, service_id=?, supporter_id=?, supervisor_id=? WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

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

        try (Connection conn = DbConnection_AppSupport.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private static final String CLOSED_STATUS_FILTER
            = "i.status NOT IN ('Resuelto', 'Finished', 'Terminado')";

    //no asignados y no resueltos
    public ArrayList<Issue> getAvailableIssuesBySupporterId(int supporterId)
            throws SQLException, ClassNotFoundException {
        ArrayList<Issue> list = new ArrayList<>();
        String sql = "SELECT i.*, s.name AS service_name "
                + "FROM Issue i "
                + "INNER JOIN SupporterService ss ON ss.service_id = i.service_id "
                + "LEFT JOIN Service s ON i.service_id = s.id "
                + "WHERE ss.supporter_id = ? "
                + "AND i.supporter_id IS NULL "
                + "AND " + CLOSED_STATUS_FILTER + " "
                + "ORDER BY i.issue_timestamp ASC";

        try (Connection conn = DbConnection_AppSupport.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supporterId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }

        return list;
    }

    //todos los issues de un suportista
    public ArrayList<Issue> getIssuesBySupporterId(int supporterId)
            throws SQLException, ClassNotFoundException {
        ArrayList<Issue> list = new ArrayList<>();
        String sql = "SELECT i.*, s.name AS service_name "
                + "FROM Issue i "
                + "LEFT JOIN Service s ON i.service_id = s.id "
                + "WHERE i.supporter_id = ? "
                + "ORDER BY i.issue_timestamp ASC";

        try (Connection conn = DbConnection_AppSupport.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supporterId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }

        return list;
    }

    // validar que el servicio es el del suportista
    public boolean supporterHasService(int supporterId, int serviceId)
            throws SQLException, ClassNotFoundException {
        String sql = "SELECT 1 FROM SupporterService WHERE supporter_id = ? AND service_id = ?";

        try (Connection conn = DbConnection_AppSupport.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supporterId);
            stmt.setInt(2, serviceId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void assignIssueToSupporter(int issueId, int supporterId)
            throws SQLException, ClassNotFoundException {

        String sql = "UPDATE Issue SET supporter_id=?, status=? WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supporterId);
            stmt.setString(2, "Asignado");
            stmt.setInt(3, issueId);

            stmt.executeUpdate();
        }
    }

    // editar el estado del issue
    public void updateAssignedIssue(int issueId, String classification,
            String status, String resolutionComment)
            throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Issue SET classification=?, status=?, resolution_comment=? WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, classification);
            stmt.setString(2, status);
            stmt.setString(3, resolutionComment);
            stmt.setInt(4, issueId);

            stmt.executeUpdate();
        }
    }

    public ArrayList<Issue> getPendingByServiceId(int serviceId)
            throws SQLException, ClassNotFoundException {

        ArrayList<Issue> list = new ArrayList<>();
        String sql = "SELECT i.*, s.name AS service_name "
                + "FROM Issue i "
                + "LEFT JOIN Service s ON i.service_id = s.id "
                + "WHERE i.service_id = ? AND i.status = 'Ingresado' "
                + "ORDER BY i.issue_timestamp ASC";

        try (Connection conn = DbConnection_AppSupport.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, serviceId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }

        return list;
    }

    public ArrayList<Issue> getBySupporterId(int supporterId)
            throws SQLException, ClassNotFoundException {

        ArrayList<Issue> list = new ArrayList<>();
        String sql = "SELECT i.*, s.name AS service_name "
                + "FROM Issue i "
                + "LEFT JOIN Service s ON i.service_id = s.id "
                + "WHERE i.supporter_id = ? "
                + "ORDER BY i.issue_timestamp ASC";

        try (Connection conn = DbConnection_AppSupport.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supporterId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }

        return list;
    }
    
    public ArrayList<Issue> getNewUnassignedIssues()
        throws SQLException, ClassNotFoundException {

    ArrayList<Issue> list = new ArrayList<>();

    String sql = "SELECT i.*, s.name AS service_name "
            + "FROM Issue i "
            + "LEFT JOIN Service s ON i.service_id = s.id "
            + "WHERE i.status = 'Ingresado' "
            + "AND i.supporter_id IS NULL "
            + "ORDER BY i.issue_timestamp ASC";

    try (Connection conn = DbConnection_AppSupport.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            list.add(map(rs));
        }
    }

    return list;
}
}
