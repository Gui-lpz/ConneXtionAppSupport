package model.data;

import java.sql.*;
import java.util.ArrayList;
import model.entities.Service;

public class ServiceData {

    private Service map(ResultSet rs) throws SQLException {
        return new Service(
                rs.getInt("id"),
                rs.getString("name")
        );
    }

    public ArrayList<Service> getAll() throws SQLException, ClassNotFoundException {
        ArrayList<Service> list = new ArrayList<>();
        String sql = "SELECT * FROM Service";

        try (Connection conn = DbConnection_AppSupport.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }

        return list;
    }

    public Service findById(int id) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM Service WHERE id=?";

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
}