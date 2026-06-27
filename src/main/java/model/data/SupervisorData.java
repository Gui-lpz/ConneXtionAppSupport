package model.data;

import java.sql.*;
import java.util.ArrayList;
import model.entities.Supervisor;

public class SupervisorData {

    // Mapea solo las columnas que existen en la tabla Supervisor
    private Supervisor map(ResultSet rs) throws SQLException {
        return new Supervisor(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("first_surname"),
                rs.getString("second_surname"),
                rs.getString("email"),
                rs.getString("password"),
                0    // service_id se obtiene aparte via SupervisorService
        );
    }

    public void add(Supervisor supervisor) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO Supervisor "
                + "(name, first_surname, second_surname, email, password) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, supervisor.getName());
            stmt.setString(2, supervisor.getFirstSurname());
            stmt.setString(3, supervisor.getSecondSurname());
            stmt.setString(4, supervisor.getEmail());
            stmt.setString(5, supervisor.getPassword());

            stmt.executeUpdate();
        }
    }

    public Supervisor login(String email, String password) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM Supervisor WHERE email = ? AND password = ?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Supervisor sv = map(rs);
                    // Obtener service_id desde SupervisorService
                    sv.setServiceId(getServiceId(conn, sv.getId()));
                    return sv;
                }
            }
        }

        return null;
    }

    // Obtiene el service_id desde la tabla de relación
    private int getServiceId(Connection conn, int supervisorId) {
        String sql = "SELECT TOP 1 service_id FROM SupervisorService WHERE supervisor_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, supervisorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("service_id");
            }
        } catch (Exception ignored) {}
        return 0;
    }

    public ArrayList<Supervisor> getAll() throws SQLException, ClassNotFoundException {
        ArrayList<Supervisor> list = new ArrayList<>();
        String sql = "SELECT * FROM Supervisor";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }

        return list;
    }

    public Supervisor findById(int id) throws SQLException, ClassNotFoundException {
       String sql = "SELECT * FROM Supervisor WHERE id = ?";

       try (Connection conn = DbConnection_AppSupport.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

           stmt.setInt(1, id);

           try (ResultSet rs = stmt.executeQuery()) {
               if (rs.next()) {
                   Supervisor supervisor = map(rs);

                   supervisor.setServiceId(getServiceId(conn, supervisor.getId()));

                   return supervisor;
               }
           }
       }

    return null;
}

    public void update(Supervisor supervisor) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE Supervisor SET name=?, first_surname=?, second_surname=?, "
                + "email=?, password=? WHERE id=?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, supervisor.getName());
            stmt.setString(2, supervisor.getFirstSurname());
            stmt.setString(3, supervisor.getSecondSurname());
            stmt.setString(4, supervisor.getEmail());
            stmt.setString(5, supervisor.getPassword());
            stmt.setInt(6, supervisor.getId());

            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException, ClassNotFoundException {
        String sql = "DELETE FROM Supervisor WHERE id = ?";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
