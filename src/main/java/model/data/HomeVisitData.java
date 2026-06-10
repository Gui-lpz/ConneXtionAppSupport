package model.data;

import java.sql.*;
import java.util.ArrayList;
import model.entities.HomeVisit;

public class HomeVisitData {

    private HomeVisit map(ResultSet rs) throws SQLException {
        Timestamp visitTimestamp = rs.getTimestamp("visit_datetime");

        return new HomeVisit(
                rs.getInt("id"),
                rs.getInt("issue_id"),
                rs.getInt("supporter_id"),
                visitTimestamp != null ? visitTimestamp.toLocalDateTime() : null,
                rs.getString("address"),
                rs.getString("contact_phone"),
                rs.getString("status"),
                rs.getString("observations")
        );
    }

    public void add(HomeVisit visit) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO HomeVisit "
                + "(issue_id, supporter_id, visit_datetime, address, contact_phone, status, observations) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, visit.getIssueId());
            stmt.setInt(2, visit.getSupporterId());
            stmt.setTimestamp(3, Timestamp.valueOf(visit.getVisitDateTime()));
            stmt.setString(4, visit.getAddress());
            stmt.setString(5, visit.getContactPhone());
            stmt.setString(6, visit.getStatus());
            stmt.setString(7, visit.getObservations());

            stmt.executeUpdate();
        }
    }

    public ArrayList<HomeVisit> getAll() throws SQLException, ClassNotFoundException {
        ArrayList<HomeVisit> list = new ArrayList<>();

        String sql = "SELECT * FROM HomeVisit ORDER BY visit_datetime ASC";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }

        return list;
    }

    public HomeVisit findById(int id) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM HomeVisit WHERE id=?";

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

    public ArrayList<HomeVisit> getByIssueId(int issueId)
            throws SQLException, ClassNotFoundException {

        ArrayList<HomeVisit> list = new ArrayList<>();

        String sql = "SELECT * FROM HomeVisit "
                + "WHERE issue_id=? "
                + "ORDER BY visit_datetime ASC";

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

    public ArrayList<HomeVisit> getBySupporterId(int supporterId)
            throws SQLException, ClassNotFoundException {

        ArrayList<HomeVisit> list = new ArrayList<>();

        String sql = "SELECT * FROM HomeVisit "
                + "WHERE supporter_id=? "
                + "ORDER BY visit_datetime ASC";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supporterId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }

        return list;
    }

    public void update(HomeVisit visit) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE HomeVisit SET "
                + "issue_id=?, supporter_id=?, visit_datetime=?, address=?, "
                + "contact_phone=?, status=?, observations=? "
                + "WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, visit.getIssueId());
            stmt.setInt(2, visit.getSupporterId());
            stmt.setTimestamp(3, Timestamp.valueOf(visit.getVisitDateTime()));
            stmt.setString(4, visit.getAddress());
            stmt.setString(5, visit.getContactPhone());
            stmt.setString(6, visit.getStatus());
            stmt.setString(7, visit.getObservations());
            stmt.setInt(8, visit.getId());

            stmt.executeUpdate();
        }
    }

    public void updateStatus(int visitId, String status)
            throws SQLException, ClassNotFoundException {

        String sql = "UPDATE HomeVisit SET status=? WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, visitId);

            stmt.executeUpdate();
        }
    }

    public void completeVisit(int visitId, String observations)
            throws SQLException, ClassNotFoundException {

        String sql = "UPDATE HomeVisit SET status=?, observations=? WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "Completada");
            stmt.setString(2, observations);
            stmt.setInt(3, visitId);

            stmt.executeUpdate();
        }
    }

    public void cancelVisit(int visitId, String observations)
            throws SQLException, ClassNotFoundException {

        String sql = "UPDATE HomeVisit SET status=?, observations=? WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "Cancelada");
            stmt.setString(2, observations);
            stmt.setInt(3, visitId);

            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException, ClassNotFoundException {
        String sql = "DELETE FROM HomeVisit WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}