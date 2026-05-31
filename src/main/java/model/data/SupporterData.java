package model.data;

import java.sql.*;
import java.util.ArrayList;
import model.entities.Supporter;

public class SupporterData {

    // Mapea solo las columnas que existen en la tabla Supporter
    private Supporter map(ResultSet rs) throws SQLException {
        return new Supporter(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("first_surname"),
                rs.getString("second_surname"),
                rs.getString("email"),
                rs.getString("password"),
                0,   // service_id se obtiene aparte via SupporterService
                rs.getInt("supervisor_id")
        );
    }

    public void add(Supporter supporter) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO Supporter "
                + "(name, first_surname, second_surname, email, password, supervisor_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, supporter.getName());
            stmt.setString(2, supporter.getFirstSurname());
            stmt.setString(3, supporter.getSecondSurname());
            stmt.setString(4, supporter.getEmail());
            stmt.setString(5, supporter.getPassword());

            if (supporter.getSupervisorId() == 0) {
                stmt.setNull(6, Types.INTEGER);
            } else {
                stmt.setInt(6, supporter.getSupervisorId());
            }

            stmt.executeUpdate();
        }
    }

    public Supporter login(String email, String password) throws SQLException, ClassNotFoundException {
        // Login solo con columnas de Supporter — sin tocar SupporterService
        String sql = "SELECT * FROM Supporter WHERE email = ? AND password = ?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Supporter sp = map(rs);
                    // Intentar obtener el service_id desde SupporterService
                    sp.setServiceId(getServiceId(conn, sp.getId()));
                    return sp;
                }
            }
        }

        return null;
    }

    // Obtiene el service_id desde la tabla de relación
    private int getServiceId(Connection conn, int supporterId) {
        String sql = "SELECT TOP 1 service_id FROM SupporterService WHERE supporter_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, supporterId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("service_id");
            }
        } catch (Exception ignored) {}
        return 0;
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
        String sql = "SELECT * FROM Supporter WHERE id = ?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }

        return null;
    }

    public ArrayList<Supporter> getByServiceId(int serviceId) throws SQLException, ClassNotFoundException {
        ArrayList<Supporter> list = new ArrayList<>();
        String sql = "SELECT s.* FROM Supporter s "
                   + "INNER JOIN SupporterService ss ON s.id = ss.supporter_id "
                   + "WHERE ss.service_id = ?";

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
                + "email=?, password=?, supervisor_id=? WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, supporter.getName());
            stmt.setString(2, supporter.getFirstSurname());
            stmt.setString(3, supporter.getSecondSurname());
            stmt.setString(4, supporter.getEmail());
            stmt.setString(5, supporter.getPassword());

            if (supporter.getSupervisorId() == 0) {
                stmt.setNull(6, Types.INTEGER);
            } else {
                stmt.setInt(6, supporter.getSupervisorId());
            }

            stmt.setInt(7, supporter.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException, ClassNotFoundException {
        String sql = "DELETE FROM Supporter WHERE id = ?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
