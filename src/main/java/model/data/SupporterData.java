package model.data;

import java.sql.*;
import java.util.ArrayList;
import model.entities.Supporter;

public class SupporterData {

    private Supporter map(ResultSet rs) throws SQLException {
        return new Supporter(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("first_surname"),
                rs.getString("second_surname"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getInt("service_id"),
                rs.getInt("supervisor_id")
        );
    }

    public void add(Supporter supporter) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO Supporter "
                + "(name, first_surname, second_surname, email, password, service_id, supervisor_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, supporter.getName());
            stmt.setString(2, supporter.getFirstSurname());
            stmt.setString(3, supporter.getSecondSurname());
            stmt.setString(4, supporter.getEmail());
            stmt.setString(5, supporter.getPassword());
            stmt.setInt(6, supporter.getServiceId());

            if (supporter.getSupervisorId() == 0) {
                stmt.setNull(7, Types.INTEGER);
            } else {
                stmt.setInt(7, supporter.getSupervisorId());
            }

            stmt.executeUpdate();
        }
    }

    public Supporter login(String email, String password) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM Supporter WHERE email=? AND password=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }

        return null;
    }

    public ArrayList<Supporter> getAll() throws SQLException, ClassNotFoundException {
        ArrayList<Supporter> list = new ArrayList<>();
        String sql = "SELECT * FROM Supporter";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }

        return list;
    }

    public Supporter findById(int id) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM Supporter WHERE id=?";

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

    public ArrayList<Supporter> getByServiceId(int serviceId) throws SQLException, ClassNotFoundException {
        ArrayList<Supporter> list = new ArrayList<>();
        String sql = "SELECT * FROM Supporter WHERE service_id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, serviceId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }

        return list;
    }

    public void update(Supporter supporter) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Supporter SET name=?, first_surname=?, second_surname=?, "
                + "email=?, password=?, service_id=?, supervisor_id=? WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, supporter.getName());
            stmt.setString(2, supporter.getFirstSurname());
            stmt.setString(3, supporter.getSecondSurname());
            stmt.setString(4, supporter.getEmail());
            stmt.setString(5, supporter.getPassword());
            stmt.setInt(6, supporter.getServiceId());

            if (supporter.getSupervisorId() == 0) {
                stmt.setNull(7, Types.INTEGER);
            } else {
                stmt.setInt(7, supporter.getSupervisorId());
            }

            stmt.setInt(8, supporter.getId());

            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException, ClassNotFoundException {
        String sql = "DELETE FROM Supporter WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}